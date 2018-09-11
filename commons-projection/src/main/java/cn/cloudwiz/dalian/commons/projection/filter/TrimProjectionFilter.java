package cn.cloudwiz.dalian.commons.projection.filter;

import cn.cloudwiz.dalian.commons.projection.ProjectionFilter;
import cn.cloudwiz.dalian.commons.projection.filter.Trim.TrimType;
import cn.cloudwiz.dalian.commons.utils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class TrimProjectionFilter implements ProjectionFilter {
    @Override
    public boolean support(Method method, Object value) {
        return AnnotationUtils.findAnnotation(method, Trim.class) != null;
    }

    @Override
    public Object doFilter(Method method, Object value) {
        TypeInformation<Object> returnType = ClassTypeInformation.fromReturnTypeOf(method);
        Trim trim = AnnotationUtils.findAnnotation(method, Trim.class);
        if(returnType.isCollectionLike()){
            Collection<?> collection = BeanUtils.asCollection(value);
            value = collection.stream()
                    .map(item -> trimItem(item, trim))
                    .collect(Collectors.toList());
        }else{
            value = trimItem(value, trim);
        }
        return value;
    }

    private Object trimItem(Object value, Trim trim) {
        if (value == null && trim.value().equals(TrimType.TO_EMPTY)) {
            return StringUtils.EMPTY;
        } else if (value != null && value instanceof String) {
            switch (trim.value()) {
                case DEFAULT:
                    return StringUtils.trim((String) value);
                case TO_NULL:
                    return StringUtils.trimToNull((String) value);
                case TO_EMPTY:
                    return StringUtils.trimToEmpty((String) value);
            }
        }
        return value;
    }
}
