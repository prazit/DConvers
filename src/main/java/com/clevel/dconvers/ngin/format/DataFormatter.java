package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

        String string;
        for (DataRow row : rows) {
            string = format(row);

            try {
                writer.write(string);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    protected abstract String format(DataRow row);

}
