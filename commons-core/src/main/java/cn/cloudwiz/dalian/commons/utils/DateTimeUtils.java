package cn.cloudwiz.dalian.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateTimeUtils {

    /**
     * 格式常量 :"yyyyMMddhhmmss"
     */
    public static final String PATTERN_DB = "yyyyMMddHHmmss";
    /**
     * 格式常量 :"yyyy-MM-dd"
     */
    public static final String PATTERN_STD_DATE = "yyyy-MM-dd";
    /**
     * 格式常量 :"yyyy-MM-dd"
     */
    public static final String PATTERN_STD_TIME = "HH:mm:ss";
    /**
     * 格式常量 :"yyyy-MM-dd hh:MM:ss"
     */
    public static final String PATTERN_STD_DATETIME = "yyyy-MM-dd HH:mm:ss";

    public static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

    public static final String JAVA_ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * 按照指定的样式返回当前时间
     *
     * @param pattern 时间样式
     * @return 返回时间字符串
     */
    public static String getCurrentDate(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date());
    }

    public static String getCurrentDateForDatabase() {
        return getCurrentDate(PATTERN_DB);
    }

    public static String formatForDatabase(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DB);
        return sdf.format(date);
    }

    public static String convertFromDBToStd(String time) {
        return convertPattern(time, PATTERN_DB, PATTERN_STD_DATETIME);
    }

    public static String convertFromStdToDB(String time) {
        return convertPattern(time, PATTERN_STD_DATETIME, PATTERN_DB);
    }

    public static String convertFromStdDayToDB(String time, boolean isendtime) {
        String dbtime = convertPattern(time, PATTERN_STD_DATE, PATTERN_DB);
        if (isendtime) {
            dbtime = dbtime.substring(0, 8) + "235959";
        } else {
            dbtime = dbtime.substring(0, 8) + "000000";
        }
        return dbtime;
    }

    public static String convertFromDBPattern(String time, String topattern) {
        return convertPattern(time, PATTERN_DB, topattern);
    }

    public static String convertFromStdPattern(String time, String topattern) {
        return convertPattern(time, PATTERN_STD_DATETIME, topattern);
    }

    public static String convertPattern(String time, String frompattern, String topattern) {
        if (time == null) return null;
        SimpleDateFormat from = new SimpleDateFormat(frompattern);
        SimpleDateFormat to = new SimpleDateFormat(topattern);
        try {
            return to.format(from.parse(time.trim()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String format(Date date, String pattern) {
        if (date == null) return null;
        if (pattern == null) {
            pattern = PATTERN_STD_DATETIME;
        }
        SimpleDateFormat from = new SimpleDateFormat(pattern);
        return from.format(date);
    }

    public static Date parseFromDB(String dbtime) throws ParseException {
        return parse(dbtime, PATTERN_DB);
    }

    public static Date parse(String dbtime, String pattern) {
        if (dbtime == null) return null;
        if (pattern == null) {
            pattern = PATTERN_STD_DATETIME;
        }
        SimpleDateFormat from = new SimpleDateFormat(pattern);
        try {
            return from.parse(dbtime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前时间，11位
     *
     * @return 11位当前时间
     */
    public static long currentTimeSeconds() {
        long timeMillis = System.currentTimeMillis();
        String temp = "" + timeMillis;
        temp = temp.substring(0, temp.length() - 3);
        return Long.parseLong(temp);
    }

    /**
     * 时间转换，long类型转换成Data类型
     *
     * @param time long类型的时间戳
     * @return Data时间类型
     */
    public static Date timeSecondsToDate(long time) {
        String temp = "" + time;
        temp = StringUtils.rightPad(temp, 13, '0');
        return new Date(Long.parseLong(temp));
    }

    public static Date getFirstDateByMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public static Date filterDate(Date date, String includePattern) {
        String format = format(date, includePattern);
        return parse(format, includePattern);
    }

    public static boolean isToday(Date date){
        String today = getCurrentDate(PATTERN_STD_DATE);
        String day = format(date, PATTERN_STD_DATE);
        return Objects.equals(today, day);
    }

    public static boolean isWholeYear(Date begin, Date end, int interval){
        if(begin == null || end == null){
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        calendar.add(Calendar.YEAR, interval);
        return calendar.getTime().equals(end);
    }

    public static boolean isCurrentMonth(Date month) {
        String currentMonth = DateTimeUtils.getCurrentDate("yyyyMM");
        String testMonth = DateTimeUtils.format(month, "yyyyMM");
        return Objects.equals(currentMonth, testMonth);
    }

    public static int getDayOfMonth(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isLastMonth(Date month){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.add(Calendar.MONTH, 1);
        String currentMonth = DateTimeUtils.getCurrentDate("yyyyMM");
        String testMonth = DateTimeUtils.format(calendar.getTime(), "yyyyMM");
        return Objects.equals(currentMonth, testMonth);
    }

    public static int getCurrentDayOfMonth(){
        Calendar instance = Calendar.getInstance();
        return instance.get(Calendar.DAY_OF_MONTH);
    }

    public static String toGMTString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(RFC1123_PATTERN, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    public static String toUTCString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(JAVA_ISO8601_PATTERN, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

}
