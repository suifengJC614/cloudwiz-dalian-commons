package cn.cloudwiz.dalian.commons.export;

import cn.cloudwiz.dalian.commons.export.excel.ExcelExportHandler;
import org.dom4j.Element;

import java.io.IOException;

public enum ExportType {

    EXCEL {
        @Override
        public ExportHandler createHandler(Element element) throws IOException {
            return new ExcelExportHandler(element);
        }
    };

    public abstract ExportHandler createHandler(Element element) throws IOException;
}
