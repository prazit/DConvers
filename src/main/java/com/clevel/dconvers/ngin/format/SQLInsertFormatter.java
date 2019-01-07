package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class SQLInsertFormatter extends DataFormatter {

    private String tableName;
    private String nameQuotes;
    private String valueQuotes;
    private String eol;

    public SQLInsertFormatter(Application application, String name, String nameQuotes, String valueQuotes, String eol) {
        super(application, name, true);
        tableName = name;
        this.nameQuotes = nameQuotes;
        this.valueQuotes = valueQuotes;
        this.eol = eol;
        outputType = "sql file";
    }

    @Override
    public String format(DataRow row) {
        String columns = "";
        String values = "";

        for (DataColumn column : row.getColumnList()) {
            columns += nameQuotes + column.getName() + nameQuotes + ", ";
            column.setQuotes(valueQuotes);
            values += column.getQuotedValue() + ", ";
        }
        columns = columns.substring(0, columns.length() - 2);
        values = values.substring(0, values.length() - 2);

        String sqlInsert = "INSERT INTO " + nameQuotes + tableName + nameQuotes + " (" + columns + ") VALUES (" + values + ");" + eol;
        return sqlInsert;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLInsertFormatter.class);
    }

}
