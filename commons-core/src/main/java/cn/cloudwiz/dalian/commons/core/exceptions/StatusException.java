package cn.cloudwiz.dalian.commons.core.exceptions;

public class StatusException extends Exception{

	private static final long serialVersionUID = 5847658059134419972L;

	public String statusInfo;

	public StatusException() {
        super();
	}
	
	public StatusException(String msg) {
		super(msg);
	}
	
	public StatusException(Throwable t) {
		super(t);
	}
	
	public StatusException(String msg, Throwable t) {
		super(msg, t);
	}

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

}