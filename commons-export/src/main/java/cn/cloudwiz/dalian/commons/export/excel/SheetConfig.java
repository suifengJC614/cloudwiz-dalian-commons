package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.ExportConfig;
import org.dom4j.Element;

public class SheetConfig extends LogicConfig {

    public SheetConfig(Element sheetconfig, ExportConfig config) {
        super(sheetconfig, config);
    }

    public String getName() {
        return getAttribute("name");
    }

    public int getRowFixed() {
        String rowfix = getAttribute("rowfix");
        try {
            if (rowfix != null) {
                return Integer.parseInt(rowfix);
            }
        } catch (Exception e) {
            log.warn("rowfix[" + rowfix + "] is invalid value", e);
        }
        return -1;
    }

    public int getColFixed() {
        String colfix = getAttribute("colfix");
        try {
            if (colfix != null) {
                return Integer.parseInt(colfix);
            }
        } catch (Exception e) {
            log.warn("colfix[" + colfix + "] is invalid value", e);
        }
        return -1;
    }

}
