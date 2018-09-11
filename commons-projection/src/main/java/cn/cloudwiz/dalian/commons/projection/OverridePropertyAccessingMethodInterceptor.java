package cn.cloudwiz.dalian.commons.projection;

import cn.cloudwiz.dalian.commons.core.BaseData;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.data.projection.MethodInterceptorFactory;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class OverridePropertyAccessingMethodInterceptor implements MethodInterceptor {

	private final BeanWrapper target;
	
	private Map<String, Object> overrides = new HashMap<>();
	
	public OverridePropertyAccessingMethodInterceptor(Object target) {
		Assert.notNull(target, "Proxy target must not be null!");
		this.target = new DirectFieldAccessFallbackBeanWrapper(target);
	}
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {

		Method method = invocation.getMethod();

		if (ReflectionUtils.isObjectMethod(method)) {
			return invocation.proceed();
		}

		PropertyDescriptor descriptor = BeanUtils.findPropertyForMethod(method);

		if (descriptor == null) {
			throw new IllegalStateException("Invoked method is not a property accessor!");
		}
		if (!isSetterMethod(method, descriptor)) {
			try{
				if(overrides.containsKey(descriptor.getName())){
					return overrides.get(descriptor.getName());
				}
				return target.getPropertyValue(descriptor.getName());
			}catch(NotReadablePropertyException e){
				String name = descriptor.getName();
				String convertName = BaseData.convertFieldName(name);
				if(ObjectUtils.notEqual(name, convertName)){
					try{
						return target.getPropertyValue(convertName);
					}catch(NotReadablePropertyException ex){}
				}
				return null;
			}
		}
		
		if (invocation.getArguments().length != 1) {
			throw new IllegalStateException("Invoked setter method requires exactly one argument!");
		}

		try{
			target.setPropertyValue(descriptor.getName(), invocation.getArguments()[0]);
		}catch(NotWritablePropertyException e){
			return overrides.put(descriptor.getName(), invocation.getArguments()[0]);
		}
		return null;
	}

	private static boolean isSetterMethod(Method method, PropertyDescriptor descriptor) {
		return method.equals(descriptor.getWriteMethod());
	}

	public static enum OverridePropertyAccessingMethodInterceptorFactory implements MethodInterceptorFactory {
		INSTANCE;

		@Override
		public MethodInterceptor createMethodInterceptor(Object source, Class<?> targetType) {
			return new OverridePropertyAccessingMethodInterceptor(source);
		}

		@Override
		public boolean supports(Object source, Class<?> targetType) {
			ClassTypeInformation<?> type = ClassTypeInformation.from(source.getClass());
			return !Map.class.isInstance(source) && !type.isCollectionLike();
		}
		
	}
	
}
