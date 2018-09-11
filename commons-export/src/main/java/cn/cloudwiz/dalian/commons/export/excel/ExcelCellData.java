package cn.cloudwiz.dalian.commons.export.excel;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelCellData {

    private Object value;

    private boolean wrapText;

    private final int row;

    private final int col;

    private boolean number;

    private String formatPattern;

    private CellRangeAddress cellRange;

    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;

    public ExcelCellData(int row, int col) {
        this(row, col, null);
    }

    public ExcelCellData(int row, int col, String value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
    }

    public boolean isNumber() {
        return number;
    }

    public void setNumber(boolean number) {
        this.number = number;
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public CellRangeAddress getCellRange() {
        return cellRange;
    }

    public void setCellRange(int rowspan, int colspan) {
        int lastRow = Math.max(row, row + rowspan - 1);
        int lastCol = Math.max(col, col + colspan - 1);
        this.cellRange = new CellRangeAddress(row, lastRow, col, lastCol);
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ExcelCellData)) return false;
        if (this == obj) return true;
        ExcelCellData other = (ExcelCellData) obj;
        return other.row == this.row && other.col == this.col;
    }

    @Override
    public int hashCode() {
        return 3 * Integer.valueOf(row).hashCode() + 7 * Integer.valueOf(col).hashCode();
    }

    @Override
    public String toString() {
        return row + ":" + col + " = " + value;
    }

}
