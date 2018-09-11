package cn.cloudwiz.dalian.commons.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "后台找不到相关服务，清联系运维人员")
public class ServiceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -2301898611475326077L;

    /**
     * Default constructor.
     */
    public ServiceNotFoundException() {
        super();
    }

    /**
     * Constructor that allows a specific error message to be specified.
     *
     * @param message the detail message.
     */
    public ServiceNotFoundException(String message) {
        super(message);
    }

}
