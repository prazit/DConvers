package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataRow;

public class SQLUpdateFormatter extends DataFormatter {

    public SQLUpdateFormatter() {
        super(true);
    }

    @Override
    protected String format(DataRow row) {
        // TODO: create SQL Update when needed
        return "-- update table " + row.getDataTable().getTableName() +" is in development";
    }
}
