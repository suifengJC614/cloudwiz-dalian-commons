package cn.cloudwiz.dalian.commons.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.UNAUTHORIZED, reason="权限校验错误")
public class AuthException extends Exception {
    private static final long serialVersionUID = -2139543256510053565L;

    public AuthException() {
        super();
    }

    public AuthException(String msg) {
        super(msg);
    }

    public AuthException(Throwable t) {
        super(t);
    }

    public AuthException(String msg, Throwable t) {
        super(msg, t);
    }

}
