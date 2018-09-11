package cn.cloudwiz.dalian.commons.utils;

public class HttpException extends Exception{

	private static final long serialVersionUID = -3426342780562657353L;

	private int statusCode;
	private String reasonPhrase;
	
	public HttpException(int statusCode, String reasonPhrase) {
		super();
	}
	
	public HttpException(int statusCode, String reasonPhrase, String msg) {
		super(msg);
	}
	
	public HttpException(int statusCode, String reasonPhrase, Throwable t) {
		super(t);
	}
	
	public HttpException(int statusCode, String reasonPhrase, String msg, Throwable t) {
		super(msg, t);
	}

	public int getStatusCode() {
		return statusCode;
	}
	public String getReasonPhrase() {
		return reasonPhrase;
	}
	
}
