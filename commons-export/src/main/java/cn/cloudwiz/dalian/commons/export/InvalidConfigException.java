package cn.cloudwiz.dalian.commons.export;

public class InvalidConfigException extends RuntimeException{

	private static final long serialVersionUID = -8094178989343740824L;

	public InvalidConfigException(){
		super();
	}
	
	public InvalidConfigException(String msg){
		super(msg);
	}
	
	public InvalidConfigException(Throwable t){
		super(t);
	}
	
	public InvalidConfigException(String msg, Throwable t){
		super(msg, t);
	}
	
}
