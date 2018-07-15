package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataRow;

public class SQLCreateFormatter extends DataFormatter {

    public SQLCreateFormatter() {
        super(false);
    }

    @Override
    protected String format(DataRow row) {
        return "-- create table " + row.getDataTable().getTableName() +" is in development";
    }

}
