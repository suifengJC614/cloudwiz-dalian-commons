package cn.cloudwiz.dalian.commons.core.orm.mybatis;


import cn.cloudwiz.dalian.commons.utils.JsonUtils;

public class JsonTypeHandler<T> extends AbstractStringTypeHandler<T>{

	private Class<T> type;

	public JsonTypeHandler() {}

	public JsonTypeHandler(Class<T> type) {
		this.type = type;
	}
	
	@Override
	protected String toString(T value) {
		return JsonUtils.toJson(value);
	}

	@Override
	protected T fromString(String json) {
		if(type.isArray()){
			return type.cast(JsonUtils.toArray(json, type.getComponentType()));
		}
		return JsonUtils.toBean(json, type);
	}
	
}
