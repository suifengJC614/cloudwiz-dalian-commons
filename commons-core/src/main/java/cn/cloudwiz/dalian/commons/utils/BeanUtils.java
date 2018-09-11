package cn.cloudwiz.dalian.commons.utils;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


/**
 * @author Jiwei.guo
 * <p>
 * 2013-1-28
 */
@SuppressWarnings("all")
public class BeanUtils {

    private static final Logger log = LoggerFactory.getLogger(BeanUtils.class);

    static {
        BeanUtilsBean.setInstance(new BeanUtilsBean() {
            @Override
            protected Object convert(Object value, Class type) {
                Object result = super.convert(value, type);
                if (type != null && type.isInstance(result)) {
                    return result;
                }
                return null;
            }
        });
        ConvertUtilsBean convertUtils = BeanUtilsBean.getInstance().getConvertUtils();
        convertUtils.register(false, true, 0);
    }

    public static List<PropertyDescriptor> getReadProperty(Class<?> type, Class<? extends Annotation> anntype) {
        List<PropertyDescriptor> result = new ArrayList<>();
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(type);
        if (ArrayUtils.isNotEmpty(descriptors)) {
            for (PropertyDescriptor item : descriptors) {
                Method method = item.getReadMethod();
                if (method != null) {
                    Annotation annt = AnnotationUtils.findAnnotation(method, anntype);
                    if (annt != null) {
                        result.add(item);
                    }
                }
            }
        }
        return result;
    }

    public static Class<?> findAnnotationInterface(Class<? extends Annotation> annt, Class<?> type) {
        Class<?> result = null;
        if (type == null) {
            return null;
        }
        if (type.isInterface() && type.isAnnotationPresent(annt)) {
            result = type;
        }
        if (result == null) {
            result = findAnnotationInterface(annt, type.getSuperclass());
        }
        if (result == null) {
            Class<?>[] interfaces = type.getInterfaces();
            if (ArrayUtils.isNotEmpty(interfaces)) {
                for (Class<?> item : interfaces) {
                    result = findAnnotationInterface(annt, item);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Set the specified property value to the field name of the object
     *
     * @param dest
     * @param fieldName
     * @param fieldValue
     * @throws Exception
     */
    public static void setProperty(Object dest, String fieldName, Object fieldValue) throws Exception {
        try {
            org.apache.commons.beanutils.BeanUtils.setProperty(dest, fieldName, fieldValue);
        } catch (Exception ex) {
            log.warn("set property " + fieldName + " with value " + fieldValue + " for " + dest + " error ", ex);
            throw ex;
        }
    }

    public static Map<String, Object> describe(Object bean) {
        try {
            if (bean == null) return null;
            Map<String, Object> result = new HashMap<String, Object>();
            if (bean instanceof Map) {
                Map<?, ?> orig = (Map<?, ?>) bean;
                for (Object key : orig.keySet()) {
                    result.put(Objects.toString(key, null), orig.get(key));
                }
            } else {
                for (PropertyDescriptor item : PropertyUtils.getPropertyDescriptors(bean.getClass())) {
                    Method method = item.getReadMethod();
                    if (method != null) {
                        result.put(item.getName(), method.invoke(bean));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getProperty(Object src, String fieldName) {
        try {

            return PropertyUtils.getProperty(src, fieldName);
        } catch (Exception ex) {
            log.warn("get property " + fieldName + " for " + src + " error ", ex);
            throw new RuntimeException(ex);
        }
    }

    public static boolean isReadable(Object bean, String name) {
        return PropertyUtils.isReadable(bean, name);
    }

    public static boolean isWriteable(Object bean, String name) {
        return PropertyUtils.isWriteable(bean, name);
    }

    public static Method getReadMethod(Object bean, String name) {
        try {
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, name);
            return PropertyUtils.getReadMethod(pd);
        } catch (Exception e) {
            log.error("Get bean[" + bean + "] ReadMethod[" + name + "] error", e);
            throw new IllegalArgumentException(e);
        }
    }

    public static Method findReadMethod(Class<?> typeClass, String name) {
        List<Class<?>> candidateClasses = new ArrayList<Class<?>>();
        candidateClasses.add(typeClass);
        candidateClasses.addAll(ClassUtils.getAllInterfaces(typeClass));
        for (Class<?> clazz : candidateClasses) {
            PropertyDescriptor descriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(clazz, name);
            Method readMethod = descriptor.getReadMethod();
            if (readMethod != null) {
                return readMethod;
            }
        }
        return null;
    }

    public static Method getWriteMethod(Object bean, String name) {
        try {
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, name);
            return PropertyUtils.getWriteMethod(pd);
        } catch (Exception e) {
            log.error("Get bean[" + bean + "] ReadMethod[" + name + "] error", e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * copy the field value from orig to dest for the same field name
     *
     * @param dest
     * @param orig
     * @throws Exception
     */
    public static void copyProperties(Object dest, Object orig) throws Exception {
        try {
            org.springframework.beans.BeanUtils.copyProperties(orig, dest);
        } catch (Exception ex) {
            log.warn("copy properties from + " + dest + " to " + orig + " error ", ex);
            throw ex;
        }
    }

    public static Field getField(Object bean, String name) {
        if (bean == null) return null;
        for (Field field : getFields(bean)) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public static Field getField(Class<?> beanclass, String name) {
        if (beanclass == null) return null;
        for (Field field : getFields(beanclass)) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public static Set<Field> getFields(Object bean, Class<? extends Annotation> anntype) {
        Set<Field> result = new HashSet<Field>();
        if (bean != null) {
            result.addAll(getFields(bean.getClass(), anntype));
        }
        return result;
    }

    public static Set<Field> getFields(Class<?> beanclass, Class<? extends Annotation> anntype) {
        Set<Field> result = new HashSet<Field>();
        if (beanclass != null) {
            Class<?> clazz = beanclass;
            do {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(anntype)) {
                        result.add(field);
                    }
                }
            } while ((clazz = clazz.getSuperclass()) != null);
        }
        return result;
    }

    public static Set<Field> getFields(Object bean) {
        Set<Field> result = new HashSet<Field>();
        if (bean != null) {
            result.addAll(getFields(bean.getClass()));
        }
        return result;
    }

    public static Set<Field> getFields(Class<?> beanclazz) {
        Set<Field> result = new HashSet<Field>();
        if (beanclazz != null) {
            Class<?> clazz = beanclazz;
            do {
                result.addAll(Arrays.asList(clazz.getDeclaredFields()));
            } while ((clazz = clazz.getSuperclass()) != null);
        }
        return result;
    }

    public static <T> T getBeanByType(ListableBeanFactory factory, Class<T> type) {
        List<T> list = getBeansByType(factory, type);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static <T> List<T> getBeansByType(ListableBeanFactory factory, Class<T> type) {
        List<T> result = new ArrayList<T>();
        Map<String, T> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(factory, type);
        result.addAll(beans.values());
        return result;
    }

    public static <T> Map<String, T> getBeansByTypeAsMap(ListableBeanFactory factory, Class<T> type) {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(factory, type);
    }

    public static String toString(Object bean) {
        return ReflectionToStringBuilder.toString(bean, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static byte[] toSerial(Serializable bean) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(bean);
            out.flush();
            return bout.toByteArray();
        } catch (IOException e) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public static <T> T fromSerial(byte[] data, Class<T> type) {
        if (ArrayUtils.isEmpty(data)) return null;
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            return type.cast(in.readObject());
        } catch (Exception e) {
            return null;
        }
    }

    public static class BeanA {
        private String name;
        private BeanA other;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BeanA getOther() {
            return other;
        }

        public void setOther(BeanA other) {
            this.other = other;
        }
    }

    public static Collection<?> asCollection(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Collection) {
            return (Collection<?>) source;
        } else if (source.getClass().isArray()) {
            return Arrays.asList(ObjectUtils.toObjectArray(source));
        } else {
            return Collections.singleton(source);
        }
    }

    public static Iterable<?> asIterable(Object items) {
        if (items == null) {
            return null;
        }
        Iterable<?> list;
        if (items != null && items.getClass().isArray()) {
            list = Arrays.asList((Object[]) items);
        } else if (items instanceof Iterable) {
            list = (Iterable<?>) items;
        } else {
            Map<String, Object> describe = BeanUtils.describe(items);
            list = describe.entrySet();
        }
        return list;
    }

}
