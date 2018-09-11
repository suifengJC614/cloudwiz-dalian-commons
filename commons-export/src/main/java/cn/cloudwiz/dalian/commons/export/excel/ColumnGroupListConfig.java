package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.EvaluationConfig;
import cn.cloudwiz.dalian.commons.export.ExportConfig;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashSet;

public class ColumnGroupListConfig extends EvaluationConfig implements ContentConfig {

    public static final String ELEMENT_NAME = "column-group-list";

    public ColumnGroupListConfig(Element config, ExportConfig exportConfig) {
        super(config, exportConfig);
        String name = config.getName();
        Assert.isTrue(ELEMENT_NAME.equals(name), "Element[" + name + "] is not [" + ELEMENT_NAME + "]");
    }

    public int getRowNumber(EvaluationContext context) {
        Integer result = getValueByEvaluation(Integer.class, "row-number", context);
        return Math.max(ObjectUtils.defaultIfNull(result, 0), 0);
    }

    public int getColumnNumber(EvaluationContext context) {
        Integer result = getValueByEvaluation(Integer.class, "col-number", context);
        return Math.max(ObjectUtils.defaultIfNull(result, 0), 0);
    }

    public String getVarName() {
        return getAttribute("var");
    }

    public String getItems() {
        return getAttribute("items");
    }

    public String getIndex() {
        return getAttribute("index");
    }

    public int getStepLength() {
        String step = getAttribute("step");
        try {
            if (step != null) {
                return Integer.parseInt(step);
            }
        } catch (Exception e) {
            log.warn("step[" + step + "] is invalid value", e);
        }
        return 1;
    }

    public int[] getIgnores() {
        int[] ignores = null;
        String ignore = getAttribute("ignore");
        if (StringUtils.isNotBlank(ignore)) {
            String[] split = ignore.split(",");
            HashSet<Integer> temp = new HashSet<Integer>();
            for (String item : split) {
                try {
                    temp.add(Integer.parseInt(item));
                } catch (Exception e) {
                    log.warn("invalid ignore:" + item, e);
                }
            }
            ignores = ArrayUtils.toPrimitive(temp.toArray(new Integer[temp.size()]), 0);
            Arrays.sort(ignores);
        }
        return ignores;
    }

    @Override
    protected String getChildElementName() {
        return "field";
    }

    @Override
    protected FieldConfig createChildConfig(Element element) {
        return new FieldConfig(element, getExportConfig());
    }

}
