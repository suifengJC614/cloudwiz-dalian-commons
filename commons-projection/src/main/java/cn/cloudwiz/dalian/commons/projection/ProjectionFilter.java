package cn.cloudwiz.dalian.commons.projection;

import java.lang.reflect.Method;

public interface ProjectionFilter<T> {

    public boolean support(Method method, Object value);

    public T doFilter(Method method, T value);

}
