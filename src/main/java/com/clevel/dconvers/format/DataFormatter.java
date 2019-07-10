package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.UtilBase;
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
            try {
                stringBuffer.append(string);
                if (stringBuffer.length() > 65536) {
                    print(stringBuffer, writer);
                    stringBuffer = new StringBuffer();
                }
            } catch (Exception ex) {
                error("print failed!!! unexpected exception: ", ex);
                print(stringBuffer, writer);
                return false;
            }
        }
        if (allRow) {
            progressBar.close();
        }
        /*TODO DEBUG ONLY*/ memoryLog();

        string = postFormat(dataTable);
        if (string != null) {
            stringBuffer.append(string);
        }

        return print(stringBuffer, writer);
    }

    private boolean print(StringBuffer stringBuffer, Writer writer) {
        if (stringBuffer.length() > 0) {
            //log.debug("print({})", stringBuffer.toString());

            if (!allowToWrite(stringBuffer)) {
                return false;
            }

            try {
                writer.write(stringBuffer.toString());
            } catch (IOException e) {
                error("Write buffer to file is failed: {}", e.getMessage());
                return false;
            } catch (Exception ex) {
                error("Unexpected error:", ex);
                return false;
            }

            try {
                if (moreWriter.size() > 0) {
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
        // return true allow to write, false not allow to write.
        return true;
    }

    public Writer getWriter() {
        return writer;
    }

}
