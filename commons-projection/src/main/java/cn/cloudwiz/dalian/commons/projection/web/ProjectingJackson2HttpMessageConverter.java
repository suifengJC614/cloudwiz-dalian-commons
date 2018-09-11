package cn.cloudwiz.dalian.commons.projection.web;

import cn.cloudwiz.dalian.commons.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class ProjectingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

	private ProjectionFactory projectionFactory;
	private final Map<Class<?>, Boolean> supportedTypesCache = new ConcurrentReferenceHashMap<Class<?>, Boolean>();

	/**
	 * Creates a new {@link ProjectingJackson2HttpMessageConverter} using a default {@link ObjectMapper}.
	 */
	public ProjectingJackson2HttpMessageConverter(ProjectionFactory projectionFactory) {
		this.projectionFactory = projectionFactory;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#canRead(java.lang.reflect.Type, java.lang.Class, org.springframework.http.MediaType)
	 */
	@Override
	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {

		if (!canRead(mediaType)) {
			return false;
		}

		Class<?> rawType = ResolvableType.forType(type).getRawClass();
		Boolean result = supportedTypesCache.get(rawType);

		if (result != null) {
			return result;
		}

		result = rawType.isInterface() && AnnotationUtils.findAnnotation(rawType, ProjectionBody.class) != null;
		supportedTypesCache.put(rawType, result);

		return result;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#canWrite(java.lang.Class, org.springframework.http.MediaType)
	 */
	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#read(java.lang.reflect.Type, java.lang.Class, org.springframework.http.HttpInputMessage)
	 */
	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		HttpHeaders headers = inputMessage.getHeaders();
		MediaType contentType = headers.getContentType();
        Charset charset =  ObjectUtils.defaultIfNull(contentType.getCharset(), Charset.forName("UTF-8"));
        String json = StreamUtils.copyToString((InputStream)inputMessage.getBody(), charset);
		Object source;
		if(json.startsWith("[")){
			source = JsonUtils.toBean(json, List.class);
		}else{
			source = JsonUtils.toBean(json, Map.class);
		}
		Class<?> typeClass = ResolvableType.forType(type).getRawClass();

		return projectionFactory.createProjection(typeClass, source);
	}
	
}
