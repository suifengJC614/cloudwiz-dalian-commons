package cn.cloudwiz.dalian.commons.export;

import org.dom4j.Element;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class ExportConfig extends BasicConfig implements Iterable<BasicConfig> {

    private ExpressionParser parser;
    private Map<String, Expression> expressionCache = new HashMap<>();

    protected ExportConfig(Element root) throws IOException {
        super(root);
        parser = new SpelExpressionParser();
    }

    public String getTemplate() {
        return getAttribute("template");
    }

    public Expression getExpression(String spel) {
        if (!expressionCache.containsKey(spel)) {
            Expression expression = parser.parseExpression(spel, ParserContext.TEMPLATE_EXPRESSION);
            expressionCache.put(spel, expression);
        }
        return expressionCache.get(spel);
    }
}
