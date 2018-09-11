package cn.cloudwiz.dalian.commons.export;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.expression.EvaluationContext;

public class EvaluationConfig extends BasicConfig {

    private ExportConfig exportConfig;

    public EvaluationConfig(Element config, ExportConfig exportConfig) {
        super(config);
        this.exportConfig = exportConfig;
        this.loadCache();
    }

    public ExportConfig getExportConfig() {
        return exportConfig;
    }

    @Override
    protected void loadCache() {
        if(this.exportConfig != null){
            super.loadCache();
        }
    }

    protected <T> T getValueByEvaluation(Class<T> type, String name, EvaluationContext context) {
        ExportConfig exportConfig = getExportConfig();
        String template = getAttribute(name);
        try {
            if(StringUtils.isNotEmpty(template)){
                return exportConfig.getExpression(template).getValue(context, type);
            }
        } catch (Exception e) {
            log.warn("parser el expres[" + template + "] failed", e);
        }
        return null;
    }
}
