package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLInsertFormatter extends DataFormatter {

    private String tableName;
    private String nameQuotes;
    private String valueQuotes;

    public SQLInsertFormatter(Application application, String name, String nameQuotes, String valueQuotes) {
        super(application, name, true);
        tableName = name;
        this.nameQuotes = nameQuotes;
        this.valueQuotes = valueQuotes;
        outputType = "sql file";
    }

    @Override
    public String format(DataRow row) {
        String columns = "";
        String values = "";

        for (DataColumn column : row.getColumnList()) {
            column.setQuotes(valueQuotes);
            columns += nameQuotes + column.getName() + nameQuotes + ", ";
            values += column.getQuotedValue().replaceAll("\r\n|\n\r|\n", "\\\\n") + ", ";
        }
        columns = columns.substring(0, columns.length() - 2);
        values = values.substring(0, values.length() - 2);

        String sqlInsert = "INSERT INTO " + nameQuotes + tableName + nameQuotes + " (" + columns + ") VALUES (" + values + ");\n";
        return sqlInsert;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLInsertFormatter.class);
    }

}
