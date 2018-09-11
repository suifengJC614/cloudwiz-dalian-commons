package cn.cloudwiz.dalian.commons.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.List;

public final class JsonUtils {
	
	private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
	
	private static ObjectMapper mapper;
	
	private static synchronized ObjectMapper getMapperInstance() {
		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//			mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,true);
		}
		
		return mapper;	}

	/*
	 * bean to json
	 * return null when exceptions
	 */
	public static String toJson(Object obj) {
		if(obj == null) return null;
		ObjectMapper om = getMapperInstance();
		try {
			return om.writeValueAsString(obj);
		} catch (IOException e) {
			log.error("tojson failed" + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * json数组转数组对象
	 * @param json json字符串
	 * @param clazz 数组元素类型
	 * @return 数组
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(String json, Class<T> clazz){
		if(json == null) return null;
		ObjectMapper om = getMapperInstance();
		JavaType type = om.getTypeFactory().constructParametricType(List.class, clazz);
		try {
			List<T> list = om.readValue(json, type);
			return list.toArray((T[])Array.newInstance(clazz, list.size()));
		} catch (IOException e) {
			log.error("tojson failed" + e.getMessage(), e);
			return null;
		}
	}
	
	/*
	 * json to bean
	 * return null when exceptions
	 */	
	public static <T> T toBean(String json, Class<T> valueType) {
		if(StringUtils.isBlank(json)) return null;
		ObjectMapper om = getMapperInstance();
		try {
			return valueType.cast(om.readValue(json, valueType));
		} catch (IOException e) {
			log.error("tojson failed" + e.getMessage(), e);
			return null;
		}
	}

	public static Object fromJson(String json){
        return JSON.parse(StringUtils.trimToNull(json));
    }

	public static void readBigJsonArray(InputStream bigin, BigJsonCallback callback) throws AcceptJsonException {
        readBigJsonArray(new InputStreamReader(bigin), callback);
    }

    public static void readBigJsonArray(Reader bigin, BigJsonCallback callback) throws AcceptJsonException {
        try (JSONReader reader = new JSONReader(bigin)){
            reader.startArray();
            while(reader.hasNext()){
                JSONObject object = (JSONObject)reader.readObject();
                callback.accept(object.toJSONString());
            }
            reader.endArray();
        }
    }

    @FunctionalInterface
    public static interface BigJsonCallback{
	    public void accept(String json) throws AcceptJsonException;
    }

    public static class AcceptJsonException extends Exception{
        private static final long serialVersionUID = -3208921658484073032L;

        public AcceptJsonException() {
            super();
        }

        public AcceptJsonException(String msg) {
            super(msg);
        }

        public AcceptJsonException(Throwable t) {
            super(t);
        }

        public AcceptJsonException(String msg, Throwable t) {
            super(msg, t);
        }

    }

}
