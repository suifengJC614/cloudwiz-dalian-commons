package cn.cloudwiz.dalian.commons.projection;

import cn.cloudwiz.dalian.commons.projection.OverridePropertyAccessingMethodInterceptor.OverridePropertyAccessingMethodInterceptorFactory;
import cn.cloudwiz.dalian.commons.utils.BeanUtils;
import cn.cloudwiz.dalian.commons.utils.DateTimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.util.AnnotationDetectionMethodCallback;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.*;

public class ProxyProjectionFactoryBean implements FactoryBean<ProjectionFactory>, BeanFactoryAware, BeanClassLoaderAware, ApplicationContextAware {

    private BeanFactory beanFactory;
    private ClassLoader classLoader;
    private ObjectMapper mapper;
    private boolean autoInjectConverter = true;
    private List<ProjectionConverter> converters;
    private List<ProjectionFilter> filters;
    private ApplicationContext applicationContext;
    private ProjectionFactory factory;

    protected ProjectionFactory initProjectionFactory() throws Exception {
        if (converters == null) {
            converters = new ArrayList<>();
        }
        if (autoInjectConverter) {
            converters.addAll(BeanUtils.getBeansByType(applicationContext, ProjectionConverter.class));
            AnnotationAwareOrderComparator.sort(converters);
        }
        converters.addAll(Arrays.asList(DefaultConverter.values()));

        if (filters == null) {
            filters = new ArrayList<>();
        }
        filters.addAll(BeanUtils.getBeansByType(applicationContext, ProjectionFilter.class));
        AnnotationAwareOrderComparator.sort(filters);

        SpelAwareProxyProjectionFactory factory = new SpelAwareProxyProjectionFactory() {
            private final Map<Class<?>, Boolean> typeCache = new HashMap<Class<?>, Boolean>();
            private final SpelExpressionParser parser = new SpelExpressionParser();

            private MethodInterceptor wrapSpelAccessorInterceptor(MethodInterceptor interceptor, Object source,
                                                                  Class<?> projectionType) {
                if (!typeCache.containsKey(projectionType)) {

                    AnnotationDetectionMethodCallback<Value> callback = new AnnotationDetectionMethodCallback<Value>(Value.class);
                    ReflectionUtils.doWithMethods(projectionType, callback);

                    typeCache.put(projectionType, callback.hasFoundAnnotation());
                }
                return typeCache.get(projectionType)
                        ? new SpelEvaluatingMethodInterceptor(interceptor, source, beanFactory, parser, projectionType)
                        : interceptor;
            }

            @Override
            protected MethodInterceptor postProcessAccessorInterceptor(MethodInterceptor interceptor, Object source,
                                                                       Class<?> projectionType) {

                interceptor = wrapSpelAccessorInterceptor(interceptor, source, projectionType);

                ReturnConvertMethodInterceptor result = new ReturnConvertMethodInterceptor(interceptor,
                        converters.toArray(new ProjectionConverter[converters.size()]));
                result.setFilters(filters);
                return result;
            }
        };
        factory.setBeanFactory(beanFactory);
        factory.setBeanClassLoader(classLoader);
        if (mapper == null) {
            mapper = Jackson2ObjectMapperBuilder.json().build();
        }
        factory.registerMethodInvokerFactory(OverridePropertyAccessingMethodInterceptorFactory.INSTANCE);
        factory.registerMethodInvokerFactory(new MultiSourceMethodInterceptorFactory());
        factory.registerMethodInvokerFactory(new JsonProjectingMethodInterceptorFactory(new JacksonMappingProvider(mapper)));
        return factory;
    }

    public enum DefaultConverter implements ProjectionConverter {
        NULL_PRIMITIVE {
            @Override
            public boolean canConvert(Method method, Class<?> type, Object origin) {
                return origin == null && type.isPrimitive();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Method method, Class<T> type, Object origin) {
                Object newInstance = Array.newInstance(type, 1);
                return (T) Array.get(newInstance, 0);
            }
        },
        INTEGER_TO_BOOLEAN {
            @Override
            public boolean canConvert(Method method, Class<?> type, Object origin) {
                return origin instanceof Number && (type == Boolean.TYPE || type == Boolean.class);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Method method, Class<T> type, Object origin) {
                boolean result = BooleanUtils.toBoolean(((Number) origin).intValue());
                return (T) Boolean.valueOf(result);
            }
        },
        STRING_TO_ENUM {
            @Override
            public boolean canConvert(Method method, Class<?> type, Object origin) {
                return type.isEnum() && origin instanceof String;
            }

            @Override
            public <T> T convert(Method method, Class<T> type, Object origin) {
                for (T item : type.getEnumConstants()) {
                    if (Objects.equals(((Enum<?>) item).name(), origin.toString())) {
                        return item;
                    }
                }
                return null;
            }
        },
        ORDINAL_TO_ENUM {
            @Override
            public boolean canConvert(Method method, Class<?> type, Object origin) {
                return type.isEnum() && origin instanceof Number && ((Number) origin).intValue() < type.getEnumConstants().length;
            }

            @Override
            public <T> T convert(Method method, Class<T> type, Object origin) {
                for (T item : type.getEnumConstants()) {
                    if (((Enum<?>) item).ordinal() == ((Number) origin).intValue()) {
                        return item;
                    }
                }
                return null;
            }
        },
        STRING_TO_DATE {
            @Override
            public boolean canConvert(Method method, Class<?> type, Object origin) {
                return Date.class.isAssignableFrom(type) && (StringUtils.isBlank(Objects.toString(origin, null))
                        || origin.toString().trim().matches("\\d{4}-[0-1][0-9]-[0-3][0-9]")
                        || origin.toString().trim().matches("\\d{4}-[0-1][0-9]")
                        || origin.toString().trim().matches("\\d{14}"));
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Method method, Class<T> type, Object origin) {
                try {
                    if (StringUtils.isBlank(Objects.toString(origin, null))) {
                        return null;
                    }
                    String time = origin.toString().trim();
                    if (time.matches("\\d{4}-[0-1][0-9]-[0-3][0-9]")) {
                        return (T) DateTimeUtils.parse(time, DateTimeUtils.PATTERN_STD_DATE);
                    } else if (time.matches("\\d{4}-[0-1][0-9]")) {
                        return (T) DateTimeUtils.parse(time, "yyyy-MM");
                    }
                    return (T) DateTimeUtils.parseFromDB(time);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        },
        LONG_TO_DATE {
            @Override
            public boolean canConvert(Method method, Class<?> type, Object origin) {
                return Date.class.isAssignableFrom(type) && (origin instanceof Number);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Method method, Class<T> type, Object origin) {
                Number value = (Number) origin;
                return (T) new Date(value.longValue());
            }
        }
    }

    @Override
    public ProjectionFactory getObject() throws Exception {
        if (factory == null) {
            synchronized (this) {
                if (factory == null) {
                    factory = initProjectionFactory();
                }
            }
        }
        return factory;
    }

    @Override
    public Class<?> getObjectType() {
        return ProjectionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void setConverters(List<ProjectionConverter> converters) {
        this.converters = converters;
    }

    public void setFilters(List<ProjectionFilter> filters) {
        this.filters = filters;
    }

    public void setAutoInjectConverter(boolean autoInjectConverter) {
        this.autoInjectConverter = autoInjectConverter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
