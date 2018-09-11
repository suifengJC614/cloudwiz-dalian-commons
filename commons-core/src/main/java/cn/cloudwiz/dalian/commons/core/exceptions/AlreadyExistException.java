package cn.cloudwiz.dalian.commons.core.exceptions;

public class AlreadyExistException extends Exception{

	private static final long serialVersionUID = 609982157295241221L;

	private String existInfo;
	
	public AlreadyExistException() {
		super();
	}
	
	public AlreadyExistException(String msg) {
		super(msg);
	}
	
	public AlreadyExistException(Throwable t) {
		super(t);
	}
	
	public AlreadyExistException(String msg, Throwable t) {
		super(msg, t);
	}

	public String getExistInfo() {
		return existInfo;
	}

	public void setExistInfo(String existInfo) {
		this.existInfo = existInfo;
	}
	
}
