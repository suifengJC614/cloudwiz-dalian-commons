package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.EvaluationConfig;
import cn.cloudwiz.dalian.commons.export.ExportConfig;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.expression.EvaluationContext;

import java.util.Objects;

public class FieldConfig extends EvaluationConfig {


    public FieldConfig(Element config, ExportConfig contentConfig) {
        super(config, contentConfig);
    }

    public String getWrapText() {
        return getAttribute("wrap-text", "false");
    }

    public String getDefaultValue() {
        return getAttribute("defvalue", "");
    }

    public boolean isNumberType() {
        String type = getAttribute("type", "string");
        return Objects.equals(type.toLowerCase(), "number");
    }

    public String getDataFormat(){
        return getAttribute("data-format");
    }

    public String getValue() {
        String value = getAttribute("value", "");
        if (StringUtils.isBlank(value)) {
            value = getContent();
        }
        return value;
    }

    public int getRowSpan(EvaluationContext context){
        Integer value = getValueByEvaluation(Integer.class, "row-span", context);
        return Math.max(ObjectUtils.defaultIfNull(value, 0), 0);
    }

    public int getColSpan(EvaluationContext context){
        Integer value = getValueByEvaluation(Integer.class, "col-span", context);
        return Math.max(ObjectUtils.defaultIfNull(value, 0), 0);
    }

    public String getHorizontalAlignment() {
        return getAttribute("h-align");
    }

    public String getVerticalAlignment() {
        return getAttribute("v-align");
    }

}