package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLUpdateFormatter extends DataFormatter {

    public SQLUpdateFormatter(Application application, String name) {
        super(application, name, true);

        outputType = "sql file";
    }

    @Override
    protected String format(DataRow row) {
        // TODO: (low priority) create SQL Update when needed
        return "-- update table " + row.getDataTable().getTableName() +" is in development";
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLUpdateFormatter.class);
    }
}
