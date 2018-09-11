package cn.cloudwiz.dalian.commons.export;

import org.apache.poi.ss.formula.functions.T;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExporterProxyFactory implements FactoryBean<Object>, InitializingBean, ResourceLoaderAware, ApplicationContextAware {

    private Class<?> exporterInterface;

    private Object exporterProxy;

    private ResourceLoader resourceLoader;

    private ApplicationContext applicationContext;

    public ExporterProxyFactory(Class<T> exporterInterface) {
        this.exporterInterface = exporterInterface;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Exporter exporter = exporterInterface.getAnnotation(Exporter.class);
        Resource resource = resourceLoader.getResource(exporter.value());
        Map<String, Element> elements = buildElements(resource);
        this.exporterProxy = createProxy(elements);
    }

    private Object createProxy(Map<String, Element> source) {
        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(source);
        factory.setOpaque(true);
        factory.setInterfaces(exporterInterface);
        factory.addAdvice(new DefaultMethodInvokingMethodInterceptor());
        factory.addAdvice(new ExportMethodInterceptor(source, applicationContext));

        ClassLoader classLoader = exporterInterface.getClassLoader();
        return factory.getProxy(classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader);
    }

    private Map<String, Element> buildElements(Resource resource) throws IOException, DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(resource.getInputStream());
        Element root = document.getRootElement();
        List<?> elements = root.elements();
        return elements.stream().map(item -> (Element) item).collect(Collectors.toMap(
                item -> item.attributeValue("id"),
                item -> item
        ));
    }

    @Override
    public Object getObject() throws Exception {
        return this.exporterProxy;
    }

    @Override
    public Class<?> getObjectType() {
        return this.exporterInterface;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
