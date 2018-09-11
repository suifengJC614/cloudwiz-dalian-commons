package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.BasicConfig;
import cn.cloudwiz.dalian.commons.export.EvaluationConfig;
import cn.cloudwiz.dalian.commons.export.ExportConfig;
import org.dom4j.Element;
import org.springframework.expression.EvaluationContext;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class LogicConfig extends EvaluationConfig {

    public LogicConfig(Element config, ExportConfig exportConfig) {
        super(config, exportConfig);
    }

    @Override
    protected BasicConfig createChildConfig(Element element) {
        String name = element.getName();
        if (name.equals(ColumnGroupListConfig.ELEMENT_NAME)) {
            return new ColumnGroupListConfig(element, getExportConfig());
        } else if (name.equals(CellConfig.ELEMENT_NAME)) {
            return new CellConfig(element, getExportConfig());
        } else if (name.equals(SingleRowConfig.ELEMENT_NAME)) {
            return new SingleRowConfig(element, getExportConfig());
        } else if (name.equals(ForeachConfig.ELEMENT_NAME)) {
            return new ForeachConfig(element, getExportConfig());
        }
        return super.createChildConfig(element);
    }

    public Stream<ExcelCellData> streamCellData(EvaluationContext context, Function<ContentConfig, Stream<ExcelCellData>> function) {
        List<BasicConfig> childConfigs = getChildConfigs();
        return childConfigs.stream().flatMap(item -> {
            if (item instanceof LogicConfig) {
                return ((LogicConfig) item).streamCellData(context, function);
            } else if (item instanceof ContentConfig) {
                return function.apply((ContentConfig) item);
            } else {
                return Stream.empty();
            }
        });
    }
}
