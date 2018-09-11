package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.BasicConfig;
import cn.cloudwiz.dalian.commons.export.ExportConfig;
import cn.cloudwiz.dalian.commons.utils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component(ColumnGroupListConfig.ELEMENT_NAME)
public class ColumnGroupListParser extends CellParser {

    private static final Logger log = LoggerFactory.getLogger(ColumnGroupListParser.class);

    @Override
    public List<ExcelCellData> parse(ContentConfig content, EvaluationContext context) {
        Assert.isInstanceOf(ColumnGroupListConfig.class, content, "config type class cast" + content);
        ColumnGroupListConfig config = (ColumnGroupListConfig) content;
        ExportConfig exportConfig = config.getExportConfig();

        List<ExcelCellData> result = new ArrayList<ExcelCellData>();

        Object items = exportConfig.getExpression(config.getItems()).getValue(context);
        if (items == null) return result;

        Iterable<?> list = BeanUtils.asIterable(items);

        int start = config.getRowNumber(context);
        int step = config.getStepLength();
        int column = config.getColumnNumber(context);
        String indexName = config.getIndex();
        int[] ignores = config.getIgnores();
        String varName = config.getVarName();
        List<BasicConfig> fieldList = config.getChildConfigs();

        int index = 0;
        for (Object dataitem : list) {
            context.setVariable(varName, dataitem);
            if (StringUtils.isNotEmpty(indexName)) {
                context.setVariable(indexName, index);
            }
            for (int i = 0; i < fieldList.size(); i++) {
                FieldConfig fieldconfig = (FieldConfig) fieldList.get(i);
                ExcelCellData celldata = parseCellData(fieldconfig, context, start, column + i);
                result.add(celldata);
            }
            do {
                start = start + step;
            } while (ignores != null && Arrays.binarySearch(ignores, start) >= 0);
            context.setVariable(varName, null);
            if (StringUtils.isNotEmpty(indexName)) {
                context.setVariable(indexName, null);
            }
            index++;
        }
        return result;
    }

}
