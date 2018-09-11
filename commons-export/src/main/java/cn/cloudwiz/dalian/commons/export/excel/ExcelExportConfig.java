package cn.cloudwiz.dalian.commons.export.excel;

import cn.cloudwiz.dalian.commons.export.BasicConfig;
import cn.cloudwiz.dalian.commons.export.ExportConfig;
import org.dom4j.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExcelExportConfig extends ExportConfig {

    private Map<String, BasicConfig> configs = new HashMap<>();

    public ExcelExportConfig(Element element) throws IOException {
        super(element);
    }

    @Override
    protected String getChildElementName() {
        return "sheet";
    }

    @Override
    protected SheetConfig createChildConfig(Element element) {
        return new SheetConfig(element, this);
    }

    @Override
    public Iterator<BasicConfig> iterator() {
        return getChildConfigs().iterator();
    }
}
