package cn.cloudwiz.dalian.commons.projection.web;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.TargetAware;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;

public class ProxyingHandlerMethodArgumentResolver extends org.springframework.data.web.ProxyingHandlerMethodArgumentResolver {

    private ProjectionFactory proxyFactory;

    public ProxyingHandlerMethodArgumentResolver(ObjectFactory<ConversionService> conversionService) {
        super(conversionService, true);
    }

    @Override
    protected Object createAttribute(String attributeName, MethodParameter parameter, WebDataBinderFactory binderFactory,
                                     NativeWebRequest request) throws Exception {
        Object result = super.createAttribute(attributeName, parameter, binderFactory, request);
        if(proxyFactory != null && result instanceof TargetAware){
            return proxyFactory.createProjection(parameter.getParameterType(), ((TargetAware)result).getTarget());
        }
        return result;
    }

    public void setProxyFactory(ProjectionFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }
}
