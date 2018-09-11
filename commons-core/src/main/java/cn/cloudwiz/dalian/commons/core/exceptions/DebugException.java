package cn.cloudwiz.dalian.commons.core.exceptions;

public class DebugException extends RuntimeException{

	private static final long serialVersionUID = 5180379612338871276L;

	public DebugException() {
		super();
	}
	
	public DebugException(String msg) {
		super(msg);
	}
	
	public DebugException(Throwable t) {
		super(t);
	}
	
	public DebugException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
