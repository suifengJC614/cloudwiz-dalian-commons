package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.ExportConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(CellConfig.ELEMENT_NAME)
public class CellParser implements ContentParser {

    private static final Logger log = LoggerFactory.getLogger(CellParser.class);

    @Override
    public List<ExcelCellData> parse(ContentConfig content, EvaluationContext context) {
        Assert.isInstanceOf(CellConfig.class, content, "config type class cast " + content);
        CellConfig config = (CellConfig) content;
        ExportConfig exportConfig = config.getExportConfig();

        int rowNumber = config.getRowNumber(context);
        int columnNumber = config.getColumnNumber(context);

        ExcelCellData celldata = parseCellData(config, context, rowNumber, columnNumber);
        return Arrays.asList(celldata);
    }

    protected ExcelCellData parseCellData(FieldConfig fieldconfig, EvaluationContext context, int row, int column){
        String valueTemplate = fieldconfig.getValue();
        String defvalue = fieldconfig.getDefaultValue();
        ExportConfig exportConfig = fieldconfig.getExportConfig();
        String value;
        try {
            value = exportConfig.getExpression(valueTemplate).getValue(context, String.class);
        } catch (Exception e) {
            log.warn("parser el expres[" + valueTemplate + "] failed", e);
            value = defvalue;
        }
        ExcelCellData celldata = new ExcelCellData(row, column);
        if (fieldconfig.isNumberType()) {
            value = StringUtils.isNotBlank(value) ? value.toString() : defvalue;
            celldata.setValue(StringUtils.isNotBlank(value) ? Double.parseDouble(value) : 0.0);
            String dataFormat = fieldconfig.getDataFormat();
            if(StringUtils.isNotBlank(dataFormat)){
                celldata.setFormatPattern(dataFormat);
            }
        } else {
            celldata.setValue(StringUtils.isNotBlank(value) ? value : defvalue);
        }
        celldata.setWrapText(BooleanUtils.toBoolean(fieldconfig.getWrapText()));
        celldata.setNumber(fieldconfig.isNumberType());

        int rowSpan = fieldconfig.getRowSpan(context);
        int colSpan = fieldconfig.getColSpan(context);
        if(rowSpan > 0 || colSpan > 0){
            celldata.setCellRange(rowSpan, colSpan);
        }

        Optional.ofNullable(fieldconfig.getHorizontalAlignment())
                .map(item-> EnumUtils.getEnum(HorizontalAlignment.class, item.toUpperCase()))
                .ifPresent(celldata::setHorizontalAlignment);

        Optional.ofNullable(fieldconfig.getVerticalAlignment())
                .map(item-> EnumUtils.getEnum(VerticalAlignment.class, item.toUpperCase()))
                .ifPresent(celldata::setVerticalAlignment);

        return celldata;
    }

}
