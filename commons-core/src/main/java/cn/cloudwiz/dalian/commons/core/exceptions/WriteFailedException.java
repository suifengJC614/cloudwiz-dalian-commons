package cn.cloudwiz.dalian.commons.core.exceptions;

import java.io.IOException;

public class WriteFailedException extends IOException{

	private static final long serialVersionUID = -8754779960354782862L;

	public WriteFailedException() {
		super();
	}
	
	public WriteFailedException(String msg) {
		super(msg);
	}
	
	public WriteFailedException(Throwable t) {
		super(t);
	}
	
	public WriteFailedException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
