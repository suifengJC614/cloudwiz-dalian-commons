package cn.cloudwiz.dalian.commons.export.autoconfigure;

import cn.cloudwiz.dalian.commons.export.ClassPathExporterScanner;
import cn.cloudwiz.dalian.commons.export.Exporter;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@ComponentScan("org.chaosoft.framework.export")
public class ExportAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ExportAutoConfiguration.class);

    public static class ExporterScannerRegistrar implements ImportBeanDefinitionRegistrar,
            BeanFactoryAware, ResourceLoaderAware {

        private BeanFactory beanFactory;
        private ResourceLoader resourceLoader;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            logger.debug("Searching for exporters annotated with @Exporter");

            ClassPathExporterScanner scanner = new ClassPathExporterScanner(registry);
            try {
                if (this.resourceLoader != null) {
                    scanner.setResourceLoader(this.resourceLoader);
                }


                String[] packages = getScanPackages(metadata);
                if (logger.isDebugEnabled()) {
                    for (String pkg : packages) {
                        logger.debug("Using auto-configuration base package '{}'", pkg);
                    }
                }

                scanner.setAnnotationClass(Exporter.class);
                scanner.registerFilters();
                scanner.doScan(packages);
            } catch (IllegalStateException ex) {
                logger.debug("Could not determine auto-configuration package, automatic exporter scanning disabled.", ex);
            }
        }


        private String[] getScanPackages(AnnotationMetadata metadata) {
            List<String> result = new ArrayList<>();
            Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableExport.class.getName());
            if (MapUtils.isNotEmpty(attributes) && attributes.containsKey("basePackages")) {
                String[] packages = (String[]) attributes.get("basePackages");
                result.addAll(Arrays.asList(packages));
            }
            if (result.isEmpty()) {
                result.add(ClassUtils.getPackageName(metadata.getClassName()));
            }
            return result.stream().toArray(String[]::new);
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }
    }

}
