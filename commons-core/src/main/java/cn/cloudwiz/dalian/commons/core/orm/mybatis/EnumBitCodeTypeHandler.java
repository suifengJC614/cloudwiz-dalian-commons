package cn.cloudwiz.dalian.commons.core.orm.mybatis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumBitCodeTypeHandler extends AbstractIntegerTypeHandler<Set<EnumType<Integer>>> {

    private Class<? extends EnumType<Integer>> enumClass;
    private Map<Integer, EnumType<Integer>> caches;

    @SuppressWarnings("unchecked")
    public EnumBitCodeTypeHandler(Class<? extends EnumType<Integer>> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        enumClass = clazz;
        while (enumClass != null && !enumClass.isEnum()) {
            enumClass = (Class<? extends EnumType<Integer>>) enumClass.getSuperclass();
        }
        if (enumClass == null) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " does not represent an enum type.");
        }
        EnumType<Integer>[] enums = this.enumClass.getEnumConstants();
        if (enums == null || enums.length == 0) {
            throw new IllegalArgumentException(enumClass.getSimpleName() + " have not enum item.");
        }
        caches = new HashMap<>();
        Arrays.stream(enums).forEach(item -> caches.put(item.getJdbcValue(), item));
    }

    @Override
    protected int toInteger(Set<EnumType<Integer>> value) {
        int result = 0;
        for(EnumType<Integer> item : value){
            result = result | item.getJdbcValue();
        }
        return result;
    }

    @Override
    protected Set<EnumType<Integer>> fromInteger(int code) {
        Set<EnumType<Integer>> result = caches.entrySet().stream()
                .filter(item -> (item.getKey() & code) == item.getKey())
                .map(item -> item.getValue())
                .collect(Collectors.toSet());
        return result;
    }


}
