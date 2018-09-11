package cn.cloudwiz.dalian.commons.export;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Exporter {

    public String value();

}
