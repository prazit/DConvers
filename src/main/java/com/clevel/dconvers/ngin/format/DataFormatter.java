package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.UtilBase;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import me.tongfei.progressbar.ProgressBar;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public abstract class DataFormatter extends UtilBase {

    protected boolean allRow;
    protected String outputType;
    protected Writer writer;
    protected List<Writer> moreWriter;

    public DataFormatter(Application application, String name, boolean allRow) {
        super(application, name);
        this.allRow = allRow;
        outputType = "file";
        moreWriter = new ArrayList<>();
    }

    /**
     * same data will be writen into all writers in this list, please call addMoreWriter() before print.
     */
    public void addMoreWriter(Writer writer) {
        if (writer == null) {
            return;
        }
        moreWriter.add(writer);
    }

    /**
     * Print DataTable out to the writer
     *
     * @param dataTable at least 1 row
     * @param writer    output
     * @return true is success, false is failed
     */
    public boolean print(DataTable dataTable, Writer writer) {

        this.writer = writer;

        List<DataRow> rows;
        if (allRow) {
            rows = dataTable.getRowList();
        } else {
            rows = new ArrayList<>();
            rows.add(dataTable.getRow(0));
        }

        String tableName = dataTable.getName();
        int rowCount = rows.size();
        ProgressBar progressBar = null;
        if (allRow) {
            progressBar = getProgressBar("Print table(" + tableName + ") to " + outputType, rowCount);
        }

        StringBuffer stringBuffer = new StringBuffer();
        String string;

        string = preFormat(dataTable);
        if (string != null) {
            stringBuffer.append(string);
        }

        for (DataRow row : rows) {
            if (allRow) {
                progressBar.step();
            }
            string = format(row);
            if (string == null) {
                continue;
            }
            stringBuffer.append(string);
        }
        if (allRow) {
            progressBar.close();
        }

        string = postFormat(dataTable);
        if (string != null) {
            stringBuffer.append(string);
        }

        if (stringBuffer.length() > 0) {

            if (!allowToWrite(stringBuffer)) {
                return false;
            }

            try {
                writer.write(stringBuffer.toString());
            } catch (IOException e) {
                error("Write buffer to file is failed: {}", e.getMessage());
                return false;
            }

            try {
                if(moreWriter.size() > 0){
                    for (Writer more : moreWriter) {
                        more.write(stringBuffer.toString());
                    }
                }
            } catch (IOException e) {
                error("Write buffer to more-file is failed: {}", e.getMessage());
                return false;
            }

        }

        return true;
    }

    /**
     * release all writers.
     */
    public void reset() {
        writer = null;
        moreWriter = new ArrayList<>();
    }

    protected String preFormat(DataTable dataTable) {
        // Override this method to write something by DataTable before write each DataRow
        return null;
    }

    public abstract String format(DataRow row);

    protected String postFormat(DataTable dataTable) {
        // Override this method to write something by DataTable after write each DataRow
        return null;
    }

    protected boolean allowToWrite(StringBuffer stringBuffer) {
        // this function allow you to see buffer before write to the output writer, this allow you to modify stringBuffer too.
        int lines = 0;
        int length = stringBuffer.length();
        int index = 0;

        for (index = stringBuffer.indexOf("\n"); index >= 0 && index < length; index = stringBuffer.indexOf("\n", index) + 1) {
            lines++;
        }
        log.debug("allowToWrite({} character(s), {} line break(s)).", stringBuffer.length(), lines);

        // return true allow to write, false not allow to write.
        return true;
    }

    public Writer getWriter() {
        return writer;
    }

}
