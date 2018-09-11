package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.BasicConfig;
import cn.cloudwiz.dalian.commons.export.ExportHandler;
import cn.cloudwiz.dalian.commons.utils.ExcelUtils;
import cn.cloudwiz.dalian.commons.utils.NullableMapAccessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.PaneInformation;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ExcelExportHandler implements ExportHandler, BeanFactoryAware, ResourceLoaderAware {

    private ExcelExportConfig config;
    private BeanFactory beanFactory;
    private ResourceLoader resourceLoader;
    @Autowired
    private Map<String, ContentParser> parsers;

    public ExcelExportHandler(Element element) throws IOException {
        this.config = new ExcelExportConfig(element);
    }

    @Override
    public void export(OutputStream out, Object datas) throws IOException {
        if (datas == null) return;
        Resource template = resourceLoader.getResource(config.getTemplate());
        Workbook wb = ExcelUtils.readExcel(template);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(new NullableMapAccessor());
        if (beanFactory != null) {
            context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        context.setRootObject(datas);

        int sheetconut = wb.getNumberOfSheets();
        int sheetindex = 0;
        for (BasicConfig config : config) {
            SheetConfig sheetconfig = (SheetConfig) config;
            Sheet sheet;
            if (sheetindex < sheetconut) {
                sheet = wb.getSheetAt(sheetindex);
                wb.setSheetName(sheetindex, sheetconfig.getName());
            } else {
                sheet = wb.createSheet(sheetconfig.getName());
            }
            PaneInformation pinfo = sheet.getPaneInformation();
            if (pinfo == null || !pinfo.isFreezePane()) {
                int rowfix = Math.max(0, sheetconfig.getRowFixed());
                int colfix = Math.max(0, sheetconfig.getColFixed());
                if (rowfix > 0 || colfix > 0) {
                    sheet.createFreezePane(colfix, rowfix);
                }
            }

            sheetconfig.streamCellData(context, item -> {
                String name = item.getElementName();
                ContentParser parser = parsers.get(name);
                return parser.parse(item, context).stream();
            }).forEach(celldata -> {
                int rownum = celldata.getRow();
                int colnum = celldata.getCol();
                Row row = sheet.getRow(rownum);
                if (row == null) {
                    row = sheet.createRow(rownum);
                }
                Cell cell = row.getCell(colnum);
                if (cell == null) {
                    cell = row.createCell(colnum);
                    cell.setCellType(celldata.isNumber() ? CellType.NUMERIC : CellType.STRING);
                }

                CellStyle style = wb.createCellStyle();
                CellStyle origin = cell.getCellStyle();
                if(origin != null){
                    style.cloneStyleFrom(origin);
                }

                if (celldata.isWrapText()) {
                    style.setWrapText(true);
                }

                Optional.ofNullable(celldata.getCellRange()).ifPresent(sheet::addMergedRegion);
                Optional.ofNullable(celldata.getHorizontalAlignment()).ifPresent(style::setAlignment);
                Optional.ofNullable(celldata.getVerticalAlignment()).ifPresent(style::setVerticalAlignment);

                String pattern = celldata.getFormatPattern();
                Object value = celldata.getValue();
                if (celldata.isNumber()) {
                    DataFormat format = wb.createDataFormat();
                    if (StringUtils.isNotBlank(pattern)) {
                        style.setDataFormat(format.getFormat(pattern));
                    }
                    cell.setCellValue(value == null ? 0 : ((Number) value).doubleValue());
                } else {
                    cell.setCellValue(Objects.toString(celldata.getValue()));
                }
                cell.setCellStyle(style);
            });
        }
        wb.write(out);
        out.flush();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
