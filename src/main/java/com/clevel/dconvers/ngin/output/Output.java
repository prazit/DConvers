package com.clevel.dconvers.ngin.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.ngin.AppBase;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.DataFormatter;

import java.io.*;
import java.util.List;

/**
 * Output need to act as a service, no data is stored within the output instance.
 */
public abstract class Output extends AppBase {

    public Output(Application application, String name) {
        super(application, name);
    }

    public boolean print(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = getFormatterList(outputConfig, dataTable);
        if (dataFormatterList == null) {
            return false;
        }

        Writer writer = openWriter(outputConfig, dataTable);
        if (writer == null) {
            return false;
        }

        for (DataFormatter formatter : dataFormatterList) {
            if (!formatter.print(dataTable, writer)) {
                closeWriter(outputConfig, dataTable, writer, false);
                return false;
            }
        }

        if (!closeWriter(outputConfig, dataTable, writer, true)) {
            return false;
        }
        return true;
    }

    protected abstract List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable);

    protected abstract Writer openWriter(OutputConfig outputConfig, DataTable dataTable);

    protected boolean closeWriter(OutputConfig outputConfig, DataTable dataTable, Writer writer, boolean success) {
        try {
            writer.close();
        } catch (IOException e) {
            // do nothing
        }
        return true;
    }

    protected Writer createFile(String outputFile, boolean autoCreateDir, boolean append, String charset) {
        Writer writer = tryToCreateFile(outputFile, append, charset);

        if (writer == null) {
            if (autoCreateDir && autoCreateDir(outputFile)) {
                writer = tryToCreateFile(outputFile, append, charset);
            } else {
                log.error("Create output file({}) is failed! please check directory path.", outputFile);
                application.hasWarning = true;
                try {
                    writer = new PrintWriter(new OutputStreamWriter(System.out, charset));
                } catch (UnsupportedEncodingException e1) {
                    writer = new StringWriter();
                }
            }
        }

        return writer;
    }

    private Writer tryToCreateFile(String outputFile, boolean append, String charset) {
        Writer writer;

        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFile, append), charset);
        } catch (Exception e) {
            log.debug("try to create file({}) is failed, {}", outputFile, e.getMessage());
            return null;
        }

        return writer;
    }

    protected boolean autoCreateDir(String outputFile) {
        File file = new File(outputFile);
        File parentFile = file.getParentFile();
        log.debug("try to create directory path({})", parentFile);
        return parentFile.mkdirs();
    }

}
