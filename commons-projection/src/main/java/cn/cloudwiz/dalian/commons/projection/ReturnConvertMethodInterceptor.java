package cn.cloudwiz.dalian.commons.projection;

import cn.cloudwiz.dalian.commons.utils.BeanUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.CollectionFactory;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ReturnConvertMethodInterceptor implements MethodInterceptor {

	private final MethodInterceptor delegate;
	
	private List<ProjectionConverter> converts;

	private List<ProjectionFilter> filters;
	
	public ReturnConvertMethodInterceptor(MethodInterceptor delegate) {
		this(delegate, new ProjectionConverter[0]);
	}
	
	public ReturnConvertMethodInterceptor(MethodInterceptor delegate, ProjectionConverter... converts) {
		this.delegate = delegate;
		this.converts = Arrays.asList(converts);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = delegate.invoke(invocation);
        Method method = invocation.getMethod();

        TypeInformation<?> type = ClassTypeInformation.fromReturnTypeOf(method);
        Class<?> returnType = type.getType();

        if (type.isCollectionLike() && !ClassUtils.isPrimitiveArray(returnType)) {
            return convertCollectionElements(BeanUtils.asCollection(result), type, method);
        }else if (type.isMap() && result instanceof Map) {
            return convertMapValues((Map<?, ?>) result, type, method);
        }else{
            return convert(result, returnType, method);
        }
	}

	protected Object convertCollectionElements(Collection<?> sources, TypeInformation<?> type, Method method){

	    if(sources == null){
	        return null;
        }

	    Class<?> rawType = type.getType();
        TypeInformation<?> componentType = type.getComponentType();
        Assert.notNull(componentType, "convert collection elements type is null");
        Class<?> convertType = componentType.getType();
        Collection<Object> result = CollectionFactory.createCollection(rawType.isArray() ? List.class : rawType,
                sources.size());

        for (Object source : sources) {
            result.add(convert(source, convertType, method));
        }

        if (rawType.isArray()) {
            return result.toArray((Object[]) Array.newInstance(convertType, result.size()));
        }
	    return result;
    }

    protected Map<Object, Object> convertMapValues(Map<?, ?> sources, TypeInformation<?> type, Method method) {

        Map<Object, Object> result = CollectionFactory.createMap(type.getType(), sources.size());

        TypeInformation<?> valueTypeInfo = type.getMapValueType();
        Assert.notNull(valueTypeInfo, "convert map value type is null");
        Class<?> valueType = valueTypeInfo.getType();
        for (Map.Entry<?, ?> source : sources.entrySet()) {
            result.put(source.getKey(), convert(source.getValue(), valueType, method));
        }

        return result;
    }

    protected Object convert(Object result, Class<?> returnType, Method method){
        if(result != null && returnType.isInstance(result)){
            return doFilter(method, result);
        }

        if(returnType == Void.TYPE){
            return null;
        }

        for(ProjectionConverter convertor : converts){
            if(convertor.canConvert(method, returnType, result)){
                Object convert = convertor.convert(method, returnType, result);
                if(convert != null && convert != result){
                    return doFilter(method, convert);
                }
            }
        }
        return doFilter(method, result);
    }

    @SuppressWarnings("unchecked")
	protected Object doFilter(Method method, Object value){
	    Object result = value;
        if(CollectionUtils.isNotEmpty(filters)){
            for(ProjectionFilter filter : filters){
                if(filter.support(method, value)){
                    result = filter.doFilter(method, result);
                }
            }
        }
        return result;
    }

    public void setFilters(List<ProjectionFilter> filters) {
        this.filters = filters;
    }

    public void setConverts(List<ProjectionConverter> converts) {
		this.converts = converts;
	}
	
}
