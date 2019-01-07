package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class SQLUpdateFormatter extends DataFormatter {

    private String tableName;
    private String idColumnName;
    private String nameQuotes;
    private String valueQuotes;
    private String eol;

    public SQLUpdateFormatter(Application application, String name, String nameQuotes, String valueQuotes, String eol) {
        super(application, name, true);
        tableName = name;
        this.nameQuotes = nameQuotes;
        this.valueQuotes = valueQuotes;
        this.eol = eol;
        outputType = "sql file";
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        idColumnName = dataTable.getIdColumnName();
        return null;
    }

    @Override
    public String format(DataRow row) {
        DataColumn idColumn = row.getColumn(idColumnName);
        String values = "";
        for (DataColumn column : row.getColumnList()) {
            if (idColumn.equals(column)) {
                continue;
            }

            values += nameQuotes + column.getName() + nameQuotes + " = ";
            column.setQuotes(valueQuotes);
            values += column.getQuotedValue() + ", ";
        }
        values = values.substring(0, values.length() - 2);

        String where = nameQuotes + idColumnName + nameQuotes + " = " + idColumn.getQuotedValue();

        String sqlUpdate = "UPDATE " + nameQuotes + tableName + nameQuotes + " SET " + values + " WHERE " + where + ";" + eol;
        return sqlUpdate;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLUpdateFormatter.class);
    }
}
