package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.ExportConfig;
import cn.cloudwiz.dalian.commons.utils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.expression.EvaluationContext;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ForeachConfig extends LogicConfig {

    public static final String ELEMENT_NAME = "foreach";

    public ForeachConfig(Element config, ExportConfig exportConfig) {
        super(config, exportConfig);
    }

    public String getVarName() {
        return getAttribute("var");
    }

    public Iterable<?> getItems(EvaluationContext context) {
        Object result = getValueByEvaluation(Object.class, "items", context);
        return BeanUtils.asIterable(result);
    }

    public String getIndex() {
        return getAttribute("index");
    }

    @Override
    public Stream<ExcelCellData> streamCellData(EvaluationContext context, Function<ContentConfig, Stream<ExcelCellData>> function) {
        String varName = getVarName();
        String indexName = getIndex();
        Iterable<?> items = getItems(context);
        if (items == null) {
            return Stream.empty();
        }

        int[] index = {0};
        return StreamSupport.stream(items.spliterator(), false).peek(item -> {
            context.setVariable(varName, item);
            if (StringUtils.isNotEmpty(indexName)) {
                context.setVariable(indexName, index[0]);
                index[0]++;
            }
        }).flatMap(item -> super.streamCellData(context, function));
    }
}
