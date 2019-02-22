package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLUpdateFormatter extends DataFormatter {

    private List<String> columnList;
    private boolean useColumnList;
    private String tableName;
    private String idColumnName;
    private String nameQuotes;
    private String valueQuotes;
    private String eol;

    public SQLUpdateFormatter(Application application, String name, List<String> columnList, String nameQuotes, String valueQuotes, String eol) {
        super(application, name, true);
        this.columnList = columnList;
        useColumnList = columnList != null && columnList.size() > 0;
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
        if (useColumnList) {
            return formatByColumnList(row);
        }
        return formatByOriginal(row);
    }

    private String formatByOriginal(DataRow row) {
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

    private String formatByColumnList(DataRow row) {
        String values = "";
        DataColumn column;

        for (String columnName : columnList) {
            if (idColumnName.equals(columnName)) {
                continue;
            }
            column = row.getColumn(columnName);
            if (column == null) {
                error("Column({}) is not found in Table({})", columnName, row.getDataTable().getName());
                return null;
            }
            values += nameQuotes + columnName + nameQuotes + " = ";
            column.setQuotes(valueQuotes);
            values += column.getQuotedValue() + ", ";
        }
        values = values.substring(0, values.length() - 2);

        DataColumn idColumn = row.getColumn(idColumnName);
        String where = nameQuotes + idColumnName + nameQuotes + " = " + idColumn.getQuotedValue();

        String sqlUpdate = "UPDATE " + nameQuotes + tableName + nameQuotes + " SET " + values + " WHERE " + where + ";" + eol;
        return sqlUpdate;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLUpdateFormatter.class);
    }
}
