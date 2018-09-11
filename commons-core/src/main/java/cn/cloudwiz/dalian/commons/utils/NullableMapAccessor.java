package cn.cloudwiz.dalian.commons.utils;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;

import java.util.Map;

public class NullableMapAccessor extends MapAccessor {

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        return target instanceof Map;
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        try{
            return super.read(context, target, name);
        } catch (AccessException e){
            return TypedValue.NULL;
        }

    }
}
