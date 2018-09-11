package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.BasicConfig;
import cn.cloudwiz.dalian.commons.export.ExportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Component(SingleRowConfig.ELEMENT_NAME)
public class SingleRowParser extends CellParser{

    private static final Logger log = LoggerFactory.getLogger(SingleRowParser.class);

    @Override
    public List<ExcelCellData> parse(ContentConfig content, EvaluationContext context) {
        Assert.isInstanceOf(SingleRowConfig.class, content, "config type class cast" + content);
        SingleRowConfig config = (SingleRowConfig) content;
        ExportConfig exportConfig = config.getExportConfig();

        List<ExcelCellData> result = new ArrayList<ExcelCellData>();

        int start = config.getRowNumber(context);
        int column = config.getColumnNumber(context);
        List<BasicConfig> fieldList = config.getChildConfigs();
        for (int i = 0; i < fieldList.size(); i++) {
            FieldConfig fieldconfig = (FieldConfig) fieldList.get(i);
            ExcelCellData celldata = parseCellData(fieldconfig, context, start, column + i);
            result.add(celldata);
        }
        return result;
    }

}
