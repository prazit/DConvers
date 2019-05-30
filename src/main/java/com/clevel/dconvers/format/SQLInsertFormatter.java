package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.input.DBMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.List;

public class SQLInsertFormatter extends DataFormatter {

    private List<String> columnList;
    private boolean useColumnList;
    private String tableName;
    private String nameQuotes;
    private String valueQuotes;
    private String eol;
    private DBMS dbms;
    private boolean isOracle;

    public SQLInsertFormatter(Application application, String name, String dbms, List<String> columnList, String nameQuotes, String valueQuotes, String eol) {
        super(application, name, true);
        this.dbms = DBMS.parse(dbms);
        isOracle = DBMS.ORACLE.equals(this.dbms);
        this.columnList = columnList;
        useColumnList = columnList != null && columnList.size() > 0;
        tableName = name;
        this.nameQuotes = nameQuotes;
        this.valueQuotes = valueQuotes;
        this.eol = eol;
        outputType = "sql file";
    }

    @Override
    public String format(DataRow row) {
        if (useColumnList) {
            return formatByColumnList(row);
        }
        return formatByOriginal(row);
    }

    private String formatByOriginal(DataRow row) {
        String columns = "";
        String values = "";

        String value;
        for (DataColumn column : row.getColumnList()) {
            columns += nameQuotes + column.getName() + nameQuotes + ", ";
            column.setQuotes(valueQuotes);

            value = column.getQuotedValue();
            if (isOracle && !value.equals("null") && column.getType() == Types.DATE) {
                value = "TO_DATE(" + value + ",'YYYY-MM-DD HH24:MI:SS')";
            }

            //value = value.replaceAll("\r\n|\n\r|\n", "<br/>");
            values += value + ", ";
        }
        columns = columns.substring(0, columns.length() - 2);
        values = values.substring(0, values.length() - 2);

        String sqlInsert = "INSERT INTO " + nameQuotes + tableName + nameQuotes + " (" + columns + ") VALUES (" + values + ");" + eol;
        return sqlInsert;
    }

    private String formatByColumnList(DataRow row) {
        String columns = "";
        String values = "";
        DataColumn column;

        String value;
        for (String columnName : columnList) {
            column = row.getColumn(columnName);
            if (column == null) {
                error("Column({}) is not found in Table({})", columnName, row.getDataTable().getName());
                return null;
            }
            columns += nameQuotes + columnName + nameQuotes + ", ";
            column.setQuotes(valueQuotes);

            value = column.getQuotedValue();
            if (isOracle && !value.equals("null") && column.getType() == Types.DATE) {
                value = "TO_DATE(" + value + ",'YYYY-MM-DD HH24:MI:SS')";
            }

            values += value + ", ";
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
