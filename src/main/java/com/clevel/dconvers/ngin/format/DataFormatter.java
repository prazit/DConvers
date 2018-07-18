package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public abstract class DataFormatter {

    boolean allRow;

    public DataFormatter(boolean allRow) {
        this.allRow = allRow;
    }

    /**
     * Print DataTable out to the writer
     *
     * @param dataTable at least 1 row
     * @param writer    output
     * @return true is success, false is failed
     */
    public boolean print(DataTable dataTable, Writer writer) {

        List<DataRow> rows;
        if (allRow) {
            rows = dataTable.getAllRow();
        } else {
            rows = new ArrayList<>();
            rows.add(dataTable.getRow(0));
        }

        String tableName = dataTable.getTableName();
        int rowCount = rows.size() + 1;
        ProgressBar progressBar;
        if (rowCount > 3000) {
            progressBar = new ProgressBar("Print DataTable(" + tableName + ")", rowCount, 500, System.out, ProgressBarStyle.ASCII, "K", 1000);
        } else {
            progressBar = new ProgressBar("Print DataTable(" + tableName + ")", rowCount, 500, System.out, ProgressBarStyle.ASCII, " rows", 1);
        }
        progressBar.maxHint(rowCount);

        String string = format(dataTable);
        try {
            if (string != null) {
                writer.write(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        for (DataRow row : rows) {
            progressBar.step();
            string = format(row);

            try {
                writer.write(string);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        progressBar.close();

        return true;
    }

    protected String format(DataTable dataTable) {
        // Override this method to write something by DataTable before write each DataRow
        return null;
    }

    protected abstract String format(DataRow row);

}
