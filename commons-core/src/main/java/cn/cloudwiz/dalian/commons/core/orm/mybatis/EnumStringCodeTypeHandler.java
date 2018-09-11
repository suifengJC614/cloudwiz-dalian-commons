package cn.cloudwiz.dalian.commons.core.orm.mybatis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EnumStringCodeTypeHandler<E extends EnumType<String>> extends AbstractStringTypeHandler<E>{

	private Class<E> enumClass;
	private Map<String, E> caches;

	@SuppressWarnings("unchecked")
	public EnumStringCodeTypeHandler(Class<E> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("Type argument cannot be null");
		}
		enumClass = clazz;
		while(enumClass != null && !enumClass.isEnum()){
			enumClass = (Class<E>) enumClass.getSuperclass();
		}
		if(enumClass == null){
			throw new IllegalArgumentException(clazz.getSimpleName() + " does not represent an enum type.");
		}
		E[] enums=this.enumClass.getEnumConstants();
		if(enums == null || enums.length == 0){
			throw new IllegalArgumentException(enumClass.getSimpleName() + " have not enum item.");
		}
		caches = new HashMap<>();
		Arrays.stream(enums).forEach(item -> caches.put(item.getJdbcValue(), item));
	}
	@Override
	protected String toString(E value) {
		return value.getJdbcValue();
	}
	@Override
	protected E fromString(String code) {
		if(!caches.containsKey(code)){
			throw new IllegalArgumentException("Cannot convert " + code + " to " + enumClass.getSimpleName() + " by code value.");
		}
		return caches.get(code);
	}

}
