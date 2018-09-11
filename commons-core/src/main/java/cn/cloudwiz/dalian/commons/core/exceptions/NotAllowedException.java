package cn.cloudwiz.dalian.commons.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.METHOD_NOT_ALLOWED, reason="不允许的操作")
public class NotAllowedException extends Exception {
    private static final long serialVersionUID = -6647062372545094285L;

    public NotAllowedException() {
        super();
    }

    public NotAllowedException(String msg) {
        super(msg);
    }

    public NotAllowedException(Throwable t) {
        super(t);
    }

    public NotAllowedException(String msg, Throwable t) {
        super(msg, t);
    }

}
