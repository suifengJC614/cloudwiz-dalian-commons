package cn.cloudwiz.dalian.commons.projection;

import java.lang.reflect.Method;

public interface ProjectionConverter{

	public <T> T convert(Method method, Class<T> returnType, Object origin);
	
	public boolean canConvert(Method method, Class<?> type, Object origin);
	
}
