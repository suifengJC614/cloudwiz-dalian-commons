package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.ExportConfig;
import org.apache.commons.lang3.ObjectUtils;
import org.dom4j.Element;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;

public class CellConfig extends FieldConfig implements ContentConfig {

    public static final String ELEMENT_NAME = "cell";

    public CellConfig(Element config, ExportConfig exportConfig) {
        super(config, exportConfig);
        String name = config.getName();
        Assert.isTrue(ELEMENT_NAME.equals(name), "Element["+name+"] is not ["+ELEMENT_NAME+"]");
    }

    public int getRowNumber(EvaluationContext context) {
        Integer result = getValueByEvaluation(Integer.class, "row-number", context);
        return Math.max(ObjectUtils.defaultIfNull(result, 0), 0);
    }

    public int getColumnNumber(EvaluationContext context) {
        Integer result = getValueByEvaluation(Integer.class, "col-number", context);
        return Math.max(ObjectUtils.defaultIfNull(result, 0), 0);
    }

}
