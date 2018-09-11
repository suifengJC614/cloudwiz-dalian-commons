package cn.cloudwiz.dalian.commons.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.REQUEST_TIMEOUT, reason = "登录状态过期")
public class ExpiredException extends Exception{

	private static final long serialVersionUID = 1290147803819588582L;

    public ExpiredException(String msg) {
		super(msg);
	}
	
	public ExpiredException(String msg, Throwable t) {
		super(msg, t);
	}
	
}