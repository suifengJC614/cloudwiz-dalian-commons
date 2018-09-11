package cn.cloudwiz.dalian.commons.projection;

import cn.cloudwiz.dalian.commons.utils.NullableMapAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.internal.Path;
import com.jayway.jsonpath.internal.path.PathCompiler;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.Accessor;
import org.springframework.data.projection.MethodInterceptorFactory;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonProjectingMethodInterceptorFactory implements MethodInterceptorFactory {

    private ParseContext context;
    private SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * Creates a new {@link JsonProjectingMethodInterceptorFactory} using the given {@link ObjectMapper}.
     *
     * @param mapper must not be {@literal null}.
     */
    public JsonProjectingMethodInterceptorFactory(MappingProvider mapper) {

        Assert.notNull(mapper, "MappingProvider must not be null!");

        Configuration config = Configuration.builder()//
                .options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS)//
                .mappingProvider(mapper)//
                .build();

        context = JsonPath.using(config);

    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.projection.MethodInterceptorFactory#createMethodInterceptor(java.lang.Object, java.lang.Class)
     */
    @Override
    public MethodInterceptor createMethodInterceptor(Object source, Class<?> targetType) {
        DocumentContext context;
        if (InputStream.class.isInstance(source)) {
            context = this.context.parse((InputStream) source);
        } else if (source instanceof String) {
            context = this.context.parse((String) source);
        } else {
            context = this.context.parse(source);
        }
        return new InputMessageProjecting(context);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.projection.MethodInterceptorFactory#supports(java.lang.Object, java.lang.Class)
     */
    @Override
    public boolean supports(Object source, Class<?> targetType) {

        if (InputStream.class.isInstance(source) || JSONObject.class.isInstance(source)
                || JSONArray.class.isInstance(source)) {
            return true;
        }

        return hasJsonPathAnnotation(targetType);
    }

    /**
     * Returns whether the given type contains a method with a {@link org.springframework.data.web.JsonPath} annotation.
     *
     * @param type must not be {@literal null}.
     */
    private boolean hasJsonPathAnnotation(Class<?> type) {

        for (Method method : type.getMethods()) {
            if (AnnotationUtils.findAnnotation(method, org.springframework.data.web.JsonPath.class) != null) {
                return true;
            }
        }

        return false;
    }

    private class InputMessageProjecting implements MethodInterceptor {

        private final DocumentContext context;
        private EvaluationContext evaluationContext;

        public InputMessageProjecting(DocumentContext context) {
            this.context = context;
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
            evaluationContext.addPropertyAccessor(new NullableMapAccessor());
            this.evaluationContext = evaluationContext;
        }

        /*
         * (non-Javadoc)
         * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
         */
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {

            Method method = invocation.getMethod();
            Object[] args = invocation.getArguments();

            if (ReflectionUtils.isObjectMethod(method)) {
                return method.invoke(context.json(), invocation.getArguments());
            }

            TypeInformation<Object> returnType = ClassTypeInformation.fromReturnTypeOf(method);
            ResolvableType type = ResolvableType.forMethodReturnType(method);
            boolean isCollectionResult = Collection.class.isAssignableFrom(type.getRawClass());
            type = isCollectionResult ? type : ResolvableType.forClassWithGenerics(List.class, type);

            Iterable<String> jsonPaths = getJsonPaths(method, args);

            for (String jsonPath : jsonPaths) {

                try {

                    Path path = PathCompiler.compile(jsonPath);
                    if (path.isFunctionPath()) {
                        return JsonPath.read((Object) context.json(), jsonPath);
                    }

                    if (returnType.getRequiredActualType().getType().isInterface()) {

                        List<?> result = context.read(jsonPath);
                        return result.isEmpty() ? null : JsonPath.isPathDefinite(jsonPath) ?
                                result.get(0) : result;
                    }

                    type = isCollectionResult && JsonPath.isPathDefinite(jsonPath)
                            ? ResolvableType.forClassWithGenerics(List.class, type)
                            : type;

                    List<?> result = (List<?>) context.read(jsonPath, new ResolvableTypeRef(type));

                    if (isCollectionResult && JsonPath.isPathDefinite(jsonPath)) {
                        result = (List<?>) result.get(0);
                    }

                    return isCollectionResult ? result : result.isEmpty() ? null : result.get(0);

                } catch (PathNotFoundException o_O) {
                    // continue with next path
                }
            }

            return null;
        }

        private String parseJsonPath(String jsonPath, Object[] args) {
            Expression expression = parser.parseExpression(jsonPath, ParserContext.TEMPLATE_EXPRESSION);
            return expression.getValue(evaluationContext, ArgumentsWrapper.of(args), String.class);
        }

        /**
         * Returns the JSONPath expression to be used for the given method.
         */
        private Collection<String> getJsonPaths(Method method, Object[] args) {

            org.springframework.data.web.JsonPath annotation = AnnotationUtils.findAnnotation(method,
                    org.springframework.data.web.JsonPath.class);

            if (annotation != null) {
                return Stream.of(annotation.value()).map(item -> parseJsonPath(item, args))
                        .collect(Collectors.toList());
            }

            return Collections.singletonList("$.".concat(new Accessor(method).getPropertyName()));
        }

        private class ResolvableTypeRef extends TypeRef<Object> {

            private final ResolvableType type;

            public ResolvableTypeRef(ResolvableType type) {
                this.type = type;
            }

            /*
             * (non-Javadoc)
             * @see com.jayway.jsonpath.TypeRef#getType()
             */
            @Override
            public Type getType() {
                return type.getType();
            }
        }

    }

    static class ArgumentsWrapper {
        private Object[] args;

        public Object[] getArgs() {
            return args;
        }

        public static ArgumentsWrapper of(Object[] args) {
            ArgumentsWrapper result = new ArgumentsWrapper();
            result.args = args;
            return result;
        }
    }

}
