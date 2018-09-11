package cn.cloudwiz.dalian.commons.export;

import java.io.IOException;
import java.io.OutputStream;

public interface ExportHandler {

    public void export(OutputStream out, Object datas) throws IOException;

}
