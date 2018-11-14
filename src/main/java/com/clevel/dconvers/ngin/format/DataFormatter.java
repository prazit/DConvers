package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.ngin.AppBase;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public abstract class DataFormatter extends AppBase {

    protected boolean allRow;
    protected String outputType;
    protected Writer writer;

    public DataFormatter(Application application, String name, boolean allRow) {
        super(application, name);
        this.allRow = allRow;
        outputType = "file";
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
            rows = dataTable.getAllRow();
        } else {
            rows = new ArrayList<>();
            rows.add(dataTable.getRow(0));
        }

        String tableName = dataTable.getTableName();
        int rowCount = rows.size();
        ProgressBar progressBar = null;
        if (allRow) {
            if (rowCount > Defaults.PROGRESS_SHOW_KILO_AFTER.getLongValue()) {
                progressBar = new ProgressBar("Print table(" + tableName + ") to " + outputType, rowCount, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, "K", 1000);
            } else {
                progressBar = new ProgressBar("Print table(" + tableName + ") to " + outputType, rowCount, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, " rows", 1);
            }
            progressBar.maxHint(rowCount);
        }

        String string;
        try {
            string = preFormat(dataTable);
            if (string != null) {
                writer.write(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
                writer.write(string);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (allRow) {
            progressBar.close();
        }

        try {
            string = postFormat(dataTable);
            if (string != null) {
                writer.write(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected String preFormat(DataTable dataTable) {
        // Override this method to write something by DataTable before write each DataRow
        return null;
    }

    protected abstract String format(DataRow row);

    protected String postFormat(DataTable dataTable) {
        // Override this method to write something by DataTable after write each DataRow
        return null;
    }

    public Writer getWriter() {
        return writer;
    }

}
