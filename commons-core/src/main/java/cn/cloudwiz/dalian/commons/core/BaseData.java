package cn.cloudwiz.dalian.commons.core;

import cn.cloudwiz.dalian.commons.utils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class BaseData implements Serializable, Cloneable{

	private static final long serialVersionUID = -6688394515786222416L;
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public void reset(){
		Set<Field> fields = BeanUtils.getFields(this);
		fields.parallelStream()
				.filter(item -> !Modifier.isFinal(item.getModifiers()) && !Modifier.isStatic(item.getModifiers()))
				.forEach(item -> {
					try {
						FieldUtils.writeDeclaredField(this, item.getName(), null, true);
					} catch (IllegalAccessException e) {
						throw new RuntimeException();
					}
				});
	}
	
	public Object get(String name) throws Exception{
		return BeanUtils.getProperty(this, name);
	}
	
	public void set(String name, Object value) throws Exception{
		BeanUtils.setProperty(this, name, value);
	}
	
	public Map<String, Object> toMap(){
		return BeanUtils.describe(this);
	}

    public void merge(Object... others){
		if(ArrayUtils.isNotEmpty(others)){
			PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(getClass());
			StringBuffer writableMethodName = new StringBuffer("set");
			Stream.of(pds).filter(item->{
                String propertyName = item.getName();
                if(item.getWriteMethod() == null && item.getPropertyType().isPrimitive()){
                    Class<?> type = MethodUtils.getPrimitiveWrapper(item.getPropertyType());
                    writableMethodName.delete(3, writableMethodName.length());
                    writableMethodName.append(Character.toUpperCase(propertyName.charAt(0)));
                    writableMethodName.append(propertyName.substring(1));
                    Method method = MethodUtils.getAccessibleMethod(getClass(), writableMethodName.toString(), type);
                    if(method != null){
                        try {
                            item.setWriteMethod(method);
                        } catch (IntrospectionException e) {}
                    }
                }
				return item.getWriteMethod() != null;
			}).forEach(item->{
				Object value = null;
				for(Object other : others){
					try {
						Object propValue = PropertyUtils.getProperty(other, item.getName());
						if(propValue != null){
							value = propValue;
						}
					} catch (Exception e) {
					    //没有找到属性，忽略
					}
				}
				if(value != null){
					try {
						Class<?> itemType = item.getPropertyType();
						if(itemType.isPrimitive()){
							itemType = ConvertUtils.primitiveToWrapper(itemType);
						}
						value = ConvertUtils.convert(value, itemType);
						if(itemType.isInstance(value)){
							item.getWriteMethod().invoke(this, value);
						}
					} catch (Exception e) {
						log.warn(String.format("merge property[%s] value[%s] to Type[%s] failed:" + e.getMessage(),
								item.getName(), item.getPropertyType().getName()), e);
					}
				}
			});
			
		}
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this);
	}
	
	public static <T extends BaseData> T merge(Class<T> targetType, Object... other){
		Assert.notNull(targetType, "merge basedata, targetType is null");
		try {
			T result = targetType.newInstance();
			result.merge(other);
			return result;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static String convertFieldName(String name){
		if(StringUtils.isNotEmpty(name)){
			if(name.equals("key")){
				return "primaryKey";
			}else if(name.equals("primaryKey")){
				return "key";
			}
		}
		return name;
	}

}
