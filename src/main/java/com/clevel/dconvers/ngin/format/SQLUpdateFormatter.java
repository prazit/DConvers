package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataRow;

public class SQLUpdateFormatter extends DataFormatter {

    public SQLUpdateFormatter() {
        super(true);

        outputType = "sql file";
    }

    @Override
    protected String format(DataRow row) {
        // TODO: (low priority) create SQL Update when needed
        return "-- update table " + row.getDataTable().getTableName() +" is in development";
    }
}
