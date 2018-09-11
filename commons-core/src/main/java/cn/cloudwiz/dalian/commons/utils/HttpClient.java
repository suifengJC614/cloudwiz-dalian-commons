package cn.cloudwiz.dalian.commons.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.*;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;

import javax.net.ssl.SSLContext;
import javax.servlet.http.Cookie;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class HttpClient {
	
	private static Map<HttpMethod, Class<? extends HttpRequestBase>> methodRequests;
	static{
		methodRequests = new HashMap<HttpMethod, Class<? extends HttpRequestBase>>();
		methodRequests.put(HttpMethod.PUT, HttpPut.class);
		methodRequests.put(HttpMethod.GET, HttpGet.class);
		methodRequests.put(HttpMethod.POST, HttpPost.class);
	}
	
	private CloseableHttpClient httpClient;
	private HttpClientContext context;
	private CookieStore cookieStore;
	private int timeout = -1;
	
	private HttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
		this.initContext();
	}
	
	public void initContext(){
		context = HttpClientContext.create();
		Registry<CookieSpecProvider> registry = RegistryBuilder.<CookieSpecProvider>create()
		        .register(CookieSpecs.DEFAULT, new DefaultCookieSpecProvider()).build();
		context.setCookieSpecRegistry(registry);
		cookieStore = new BasicCookieStore();
		context.setCookieStore(cookieStore);
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public void addCookies(Cookie... cookies){
		if(ArrayUtils.isNotEmpty(cookies)){
			Stream.of(cookies).parallel().forEach(cookie -> {
				BasicClientCookie clientcookie = new BasicClientCookie(cookie.getName(), cookie.getValue());				
				clientcookie.setDomain(cookie.getDomain());
				clientcookie.setPath(cookie.getPath());
				cookieStore.addCookie(clientcookie);
			});
		}
	}
	public void clearCookie(){
		cookieStore.clear();
	}
	
	public String get(String url) throws HttpException, IOException{
		return get(url, null);
	}
	
	public String get(String url, Map<String, String> headers) throws HttpException, IOException{
		return request(url, null, HttpMethod.GET, headers);
	}
	
	public String post(String url) throws HttpException, IOException{
		return post(url, null, null);
	}
	
	public String post(String url, Map<String, String> headers) throws HttpException, IOException{
		return post(url, null, headers);
	}
	
	public String post(String url, Object body) throws HttpException, IOException{
		return post(url, body, null);
	}
	
	public String post(String url, Object body, Map<String, String> headers) throws HttpException, IOException{
		return request(url, body, HttpMethod.POST, headers);
	}
	
	public String put(String url) throws HttpException, IOException{
		return put(url, null, null);
	}
	
	public String put(String url, Map<String, String> headers) throws HttpException, IOException{
		return put(url, null, headers);
	}
	
	public String put(String url, Object body) throws HttpException, IOException{
		return put(url, body, null);
	}
	
	public String put(String url, Object body, Map<String, String> headers) throws HttpException, IOException{
		return request(url, body, HttpMethod.PUT, headers);
	}
	
	public String request(String url, Object body, HttpMethod method) throws HttpException, IOException{
		return request(url, body, HttpMethod.PUT, null);
	}
	
	public String request(String url, Object body, HttpMethod method, Map<String, String> headers) throws HttpException, IOException{
		if(StringUtils.isBlank(url)){
            return null;
        }
		url = url.trim();
		HttpRequestBase request = createRequest(url, method);
		if(request == null){
			return null;
		}
		if(request instanceof HttpEntityEnclosingRequest){
			HttpEntity httpEntity = createEntity(body);
			if(httpEntity != null){
				((HttpEntityEnclosingRequest)request).setEntity(httpEntity);
			}
		}
		
		if(timeout > 0){
			Builder builder = RequestConfig.custom();
			builder.setConnectTimeout(timeout);
			builder.setConnectionRequestTimeout(timeout);
			builder.setSocketTimeout(timeout);
			request.setConfig(builder.build());
		}
		
		if(headers != null && !headers.isEmpty()){
			headers.entrySet().parallelStream().forEach(entry->{
				request.addHeader(entry.getKey(), entry.getValue());
			});
		}
		
		CloseableHttpResponse response = httpClient.execute(request, context);
		
		return responseToString(response);
	}
	
	protected HttpRequestBase createRequest(String url, HttpMethod method) {
		Class<? extends HttpRequestBase> clazz = methodRequests.get(method);
		if(clazz != null){
			try{
				Constructor<? extends HttpRequestBase> constructor = clazz.getConstructor(String.class);
				return constructor.newInstance(url);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	protected HttpEntity createEntity(Object body) {
		if(body == null) return null;
		if(body instanceof Map){
			List<NameValuePair> pairs = null;
			Map<?, ?> bodyMap = (Map<?, ?>) body;
			if (bodyMap != null && !bodyMap.isEmpty()) {
				pairs = new ArrayList<NameValuePair>(bodyMap.size());
				for (Entry<?, ?> entry : bodyMap.entrySet()) {
					Object key = entry.getKey();
					Object value = entry.getValue();
					if (key != null && value != null) {
						pairs.add(new BasicNameValuePair(key.toString(), value.toString()));
					}
				}
			}
			return new UrlEncodedFormEntity(pairs,Charset.forName("UTF-8"));
		}else if(body instanceof byte[]){
			return new ByteArrayEntity((byte[])body);
		}else if(body instanceof InputStream){
			return new InputStreamEntity((InputStream)body);
		}else if(body instanceof Serializable){
			return new SerializableEntity((Serializable)body);
		}else if(body instanceof File){
			return new FileEntity((File)body);
		}else{
			return new StringEntity(body.toString(), Charset.forName("UTF-8"));
		}
	}
	
	protected String responseToString(CloseableHttpResponse response) throws HttpException, IOException{
		StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        HttpEntity entity = response.getEntity();
        String result = null;
        if (entity != null){
            result = EntityUtils.toString(entity, "utf-8");
        }
        EntityUtils.consume(entity);
        response.close();
    	if (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
			throw new HttpException(statusCode, statusLine.getReasonPhrase(), "HttpClient, error status code :" + statusCode
					+ ", reason:" + statusLine.getReasonPhrase()+", resutlt:"+result);
		}
        return result;
	}
	
	public static HttpClient getHttpClient(){
		return new HttpClient(HttpClients.createDefault());
	}
	
	public static HttpClient getHttpClient(KeyStore keystore, String pwd, String... protocols){
		try {
			HttpClientBuilder builder = HttpClients.custom();
			SSLContext sslcontext = SSLContexts.custom()
					.loadKeyMaterial(keystore, pwd.toCharArray()).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
					protocols, null,
					SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			builder.setSSLSocketFactory(sslsf);
			return new HttpClient(builder.build());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String buildBasicAuthorization(String uname, String pwd){
        String authCode = Base64.encodeBase64String(String.format("%s:%s", uname, pwd).getBytes(Charset.forName("UTF-8")));
        return String.format("Basic %s", authCode);
    }
	
}
