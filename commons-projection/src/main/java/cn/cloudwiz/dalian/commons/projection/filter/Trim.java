package cn.cloudwiz.dalian.commons.projection.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trim {

    public static enum TrimType {DEFAULT, TO_NULL, TO_EMPTY}

    public TrimType value() default TrimType.DEFAULT;

}
