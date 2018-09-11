package cn.cloudwiz.dalian.commons.projection;

import cn.cloudwiz.dalian.commons.core.BaseData;
import cn.cloudwiz.dalian.commons.utils.BeanUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.projection.Accessor;
import org.springframework.data.projection.MethodInterceptorFactory;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MultiSourceMethodInterceptorFactory implements MethodInterceptorFactory {

	@Override
	public boolean supports(Object source, Class<?> targetType) {
		ClassTypeInformation<?> type = ClassTypeInformation.from(source.getClass());
		Class<?> rawType = type.getType();
		return targetType.isInterface() && type.isCollectionLike() && !ClassUtils.isPrimitiveArray(rawType);
	}
	
	@Override
	public MethodInterceptor createMethodInterceptor(Object source, Class<?> targetType) {
		return new MultiSourceMethodInterceptor();
	}
	
	public class MultiSourceMethodInterceptor implements MethodInterceptor {

		private Map<String, Object> selfData = new HashMap<>();

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			
			Method method = invocation.getMethod();
			Object[] args = invocation.getArguments();
			Collection<?> collection = BeanUtils.asCollection(invocation.getThis());

			if(ReflectionUtils.isObjectMethod(method)){
				return method.invoke(collection, args);
			}

			Accessor accessor = new Accessor(method);
			String name = accessor.getPropertyName();
			Object result = null;
			if(accessor.isGetter() && selfData.containsKey(name)){
				return selfData.get(name);
			}
			for(Object item : collection){
				if(item != null){
					if(method.getDeclaringClass().isInstance(item)){
						result = method.invoke(item, args);
						if(result != null || !accessor.isGetter()){
							return result;
						}
					}
					if(accessor.isGetter()){
						try{
							result = PropertyUtils.getProperty(item, name);
							if(result != null){
								return result;
							}
						}catch(Exception e){
							String convertName = BaseData.convertFieldName(name);
							if(ObjectUtils.notEqual(convertName, name)){
								try{
									return PropertyUtils.getProperty(item, convertName);
								}catch(Exception ignore){}
							}
						}
					}
				}
			}
			if(accessor.isSetter() && args.length == 1){
				selfData.put(name, args[0]);
			}
			return result;
		}
		
	}

}
