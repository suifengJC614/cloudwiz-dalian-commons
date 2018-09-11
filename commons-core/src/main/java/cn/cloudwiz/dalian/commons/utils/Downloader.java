package cn.cloudwiz.dalian.commons.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class Downloader {

    private static final int TIME_OUT = (int) TimeUnit.MINUTES.toMillis(5);

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);

    private WeakHashMap<String, Object> cache = new WeakHashMap<>();

    private Downloader() {}

    private static Downloader instance = new Downloader();

    public static Downloader getInstance(){
        return instance;
    }

    public boolean download(String path, File targetFile) throws IOException{
        return download(path, targetFile, FileUtils.ONE_MB * 10, 10);
    }

    public boolean download(String path, File targetFile, long limitBytes, int maxThread) throws IOException {
        Assert.hasText(path, "download path is empty");
        Assert.notNull(targetFile, "download target file cannot be null");

        if (targetFile.exists() && targetFile.isFile()) {
            FileUtils.deleteQuietly(targetFile);
        }
        targetFile.createNewFile();

        long totalLength = getDownloadLength(path);
        try (RandomAccessFile raf = new RandomAccessFile(targetFile, "rwd")) {
            raf.setLength(totalLength);
        }



        List<DownloadPart> parts = new ArrayList<>();
        if (!isSupportMultiThread(path)) {//单线程下载
            HttpURLConnection connection = (HttpURLConnection)new URL(path).openConnection();
            parts.add(new DownloadPart(connection, targetFile, 0));
        } else if (totalLength / limitBytes > maxThread) {//按照最大线程数来分
            int surplusLength = (int)(totalLength % maxThread);
            long block = (totalLength - totalLength % maxThread)/maxThread;
            for (int i = 0; i < maxThread; i++) {
                long start = block * i;
                long end = i == maxThread - 1? 0 : (start + block - 1);
                HttpURLConnection connection = (HttpURLConnection)new URL(path).openConnection();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + (end == 0 ? "" : end));
                parts.add(new DownloadPart(connection, targetFile, start));
            }
        } else {//按照limit来分线程
            long start = 0;
            do{
                long end = start + limitBytes - 1;
                if(end >= totalLength){
                    end = 0;
                }
                HttpURLConnection connection = (HttpURLConnection)new URL(path).openConnection();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + (end == 0 ? "" : end));
                parts.add(new DownloadPart(connection, targetFile, start));
                start = start + limitBytes;
            }while (start < totalLength);
        }

        CountDownLatch countDown = new CountDownLatch(parts.size());
        parts.forEach(item -> {
            item.setCountDown(countDown);
            new Thread(item).start();
        });
        try {
            countDown.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (DownloadPart part : parts) {
            if (!part.isSuccess()){
                return false;
            }
        }
        return true;
    }

    public boolean isSupportMultiThread(String path) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(path).openConnection();
            connection.setReadTimeout(TIME_OUT);
            String acceptRanges = connection.getHeaderField("Accept-Ranges");
            return Objects.equals(acceptRanges, "bytes");
        } finally {
            IOUtils.close(connection);
        }
    }

    public long getDownloadLength(String path) throws IOException {
        String cacheKey = "ContentLength_" + path;
        Object result = cache.get(cacheKey);
        if(result == null || !(result instanceof Number)){
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(path).openConnection();
                connection.setReadTimeout(TIME_OUT);
                long length = 0, index = 0;
                while ((length = connection.getContentLength()) < 0){
                    index++;
                    if(index > 3){
                        throw new IOException(String.format("path[%s] read timeout", path));
                    }
                }
                result = length;
                cache.put(cacheKey, result);
            } finally {
                IOUtils.close(connection);
            }
        }
        return ((Number)result).longValue();
    }

    public String getContentType(String path) throws IOException{
        String cacheKey = "ContentType_" + path;
        Object result = cache.get(cacheKey);
        if(result == null || !(result instanceof String)){
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(path).openConnection();
                connection.setReadTimeout(TIME_OUT);
                result = connection.getContentType();
                cache.put(cacheKey, result);
            } finally {
                IOUtils.close(connection);
            }
        }
        return (String) result;
    }

    public boolean isVaild(String path) throws IOException {
        String cacheKey = "vaild_" + path;
        Object result = cache.get(cacheKey);
        if(result == null || !(result instanceof Boolean)){
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(path).openConnection();
                connection.setReadTimeout(TIME_OUT);

                int code = connection.getResponseCode();
                result = code == HttpStatus.SC_OK;
                cache.put(cacheKey, result);
            } finally {
                IOUtils.close(connection);
            }
        }
        return (Boolean) result;
    }



    private class DownloadPart implements Runnable {

        private HttpURLConnection connection;
        private File targetFile;
        private long start;
        private boolean success;
        private CountDownLatch countDown;

        public DownloadPart(HttpURLConnection connection, File targetFile, long start) {
            this.connection = connection;
            this.targetFile = targetFile;
            this.start = start;
        }

        @Override
        public void run() {
            try (RandomAccessFile raf = new RandomAccessFile(targetFile, "rwd")){
                raf.seek(start);

                connection.setReadTimeout(TIME_OUT);
                InputStream input = connection.getInputStream();

                byte[] buffer = new byte[StreamUtils.BUFFER_SIZE];
                int len = 0;
                while((len = input.read(buffer)) >= 0){
                    raf.write(buffer, 0, len);
                }
                success = true;
            } catch (Exception e) {
                log.error("download part failed", e);
                success = false;
            } finally {
                if (countDown != null){
                    countDown.countDown();
                }
                IOUtils.close(connection);
            }
        }

        public boolean isSuccess() {
            return success;
        }

        public void setCountDown(CountDownLatch countDown) {
            this.countDown = countDown;
        }
    }
}
