package cn.cloudwiz.dalian.commons.export.excel;

import org.springframework.expression.EvaluationContext;


public interface ContentConfig {

    public int getRowNumber(EvaluationContext context);

    public int getColumnNumber(EvaluationContext context);

    public String getElementName();

}
