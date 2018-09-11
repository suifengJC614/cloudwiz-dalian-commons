package cn.cloudwiz.dalian.commons.core.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(CoreAutoConfiguration.class)
public @interface EnableCloudwizCommons {
}
