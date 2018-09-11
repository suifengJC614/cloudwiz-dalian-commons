package cn.cloudwiz.dalian.commons.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ExcelUtils {

    public final static String EXCEL_XLS = "xls";
    public final static String EXCEL_XLSX = "xlsx";

    public static Workbook readExcel(Resource resource) throws IOException {
        if (resource == null) return null;
        Assert.isTrue(resource.isReadable(), String.format("read excel[%s], but cannot read!!!", resource.getURL().toString()));
        String filename = resource.getFilename();
        return readExcel(filename, resource.getInputStream());
    }

    public static Workbook readExcel(String fileName, InputStream resource) throws IOException {
        if (resource == null) return null;
        if (StringUtils.endsWithIgnoreCase(fileName, EXCEL_XLSX)) {
            return new XSSFWorkbook(resource);
        } else if (StringUtils.endsWithIgnoreCase(fileName, EXCEL_XLS)) {
            return new HSSFWorkbook(resource);
        }
        throw new IllegalArgumentException(String.format("unrecognized file types[%s].", fileName));
    }

    public static String getStringCellValue(Row row, Integer colnum) {
        if (row != null && colnum != null) {
            Cell cell = row.getCell(colnum);
            return StringUtils.trimToNull(getStringCellValue(cell));
        }
        return null;
    }

    public static String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        Object value = null;
        switch (cell.getCellTypeEnum()) { // 根据cell中的类型来输出数据
            case NUMERIC:
                value = cell.getNumericCellValue();
                if (value instanceof Number) {
                    Number num = (Number) value;
                    if (num.doubleValue() == num.intValue()) {
                        value = num.intValue();
                    }
                }
                break;
            case STRING:
                value = cell.getStringCellValue();
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case FORMULA:
                value = cell.getCellFormula();
                break;
            case BLANK:
                value = "";
                break;
            default:
                value = "unsuported sell type " + cell.getCellTypeEnum();
                break;
        }
        return StringUtils.trimToEmpty(value.toString());
    }

    public static Map<String, Object> getMergedRegionValues(Sheet sheet) {
        Map<String, Object> result = new HashMap<String, Object>();
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int fc = ca.getFirstColumn();
            int lc = ca.getLastColumn();
            int fr = ca.getFirstRow();
            int lr = ca.getLastRow();
            Cell cell = sheet.getRow(fr).getCell(fc);
            String value = getStringCellValue(cell);
            for (int r = fr; r <= lr; r++) {
                for (int c = fc; c <= lc; c++) {
                    result.put(r + "," + c, value);
                }
            }
        }
        return result;
    }

}
