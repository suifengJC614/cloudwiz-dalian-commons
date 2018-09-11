package cn.cloudwiz.dalian.commons.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code=HttpStatus.NOT_FOUND, reason="数据没有找到")
public class DataNotFoundException extends RuntimeException{

	private static final long serialVersionUID = 2933987769269823851L;

	public DataNotFoundException() {
		super();
	}
	
	public DataNotFoundException(String msg) {
		super(msg);
	}
	
	public DataNotFoundException(Throwable t) {
		super(t);
	}
	
	public DataNotFoundException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
