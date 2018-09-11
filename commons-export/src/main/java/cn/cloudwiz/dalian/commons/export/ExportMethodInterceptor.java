package cn.cloudwiz.dalian.commons.export;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExportMethodInterceptor implements MethodInterceptor {

    protected Map<String, Element> source;
    private Map<String, ExportHandler> handlers = new HashMap<>();
    private ApplicationContext context;

    public ExportMethodInterceptor(Map<String, Element> source, ApplicationContext context) {
        this.source = source;
        this.context = context;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (ReflectionUtils.isObjectMethod(method)) {
            return invocation.proceed();
        }

        ExportHandler exportHandler = getExportHandler(method);
        Assert.notNull(exportHandler, String.format("not found Exporter[%s] method[%s]",
                method.getDeclaringClass().getName(), method.getName()));

        Class<?> returnType = method.getReturnType();
        Object[] args = invocation.getArguments();

        OutputStream output = null;
        boolean paramsOutput = true;
        if (returnType.equals(Void.TYPE)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                if (OutputStream.class.isAssignableFrom(parameterTypes[i]) && i < args.length) {
                    output = (OutputStream) args[i];
                }
            }
        } else if (byte[].class.equals(returnType) || Byte[].class.equals(returnType)
                || InputStream.class.isAssignableFrom(returnType)) {
            output = new ByteArrayOutputStream();
            paramsOutput = false;
        }

        Assert.notNull(output, "Export Method must be one outout parameter, or input/byte[] return type");

        exportHandler.export(output, ArgumentsWrapper.of(args));

        if (!paramsOutput) {
            ByteArrayOutputStream out = (ByteArrayOutputStream) output;
            if (byte[].class.equals(returnType)) {
                return out.toByteArray();
            } else if (Byte[].class.equals(returnType)) {
                return ArrayUtils.toObject(out.toByteArray());
            } else {
                return new ByteArrayInputStream(out.toByteArray());
            }
        }
        return null;
    }

    private ExportHandler getExportHandler(Method method) throws IOException {
        String name = method.getName();
        if (!handlers.containsKey(name) && source.containsKey(name)) {
            Element element = source.get(name);
            try {
                ExportType exportType = ExportType.valueOf(element.getName().toUpperCase());
                ExportHandler handler = exportType.createHandler(element);
                AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
                beanFactory.initializeBean(handler, null);
                beanFactory.autowireBean(handler);
                handlers.put(name, handler);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(String.format("unknow export element name[%s]",
                        element.getName()), e);
            }
        }
        return handlers.get(name);
    }

    public static class ArgumentsWrapper {
        private Object[] args;

        private ArgumentsWrapper(Object[] args) {
            this.args = args;
        }

        public Object[] getArgs() {
            return args;
        }

        public static ArgumentsWrapper of(Object[] args) {
            return new ArgumentsWrapper(args);
        }
    }

}
