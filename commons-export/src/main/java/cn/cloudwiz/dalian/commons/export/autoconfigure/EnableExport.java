package cn.cloudwiz.dalian.commons.export.autoconfigure;

import cn.cloudwiz.dalian.commons.export.autoconfigure.ExportAutoConfiguration.ExporterScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ExportAutoConfiguration.class, ExporterScannerRegistrar.class})
public @interface EnableExport {

    public String[] basePackages() default {};

}
