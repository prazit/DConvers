package com.clevel.dconvers.format;

import com.clevel.dconvers.DConvers;
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

    public SQLInsertFormatter(DConvers dconvers, String name, String dbms, List<String> columnList, String nameQuotes, String valueQuotes, String eol) {
        super(dconvers, name, true);
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
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder nclob;

        String value;
        for (DataColumn column : row.getColumnList()) {
            columns.append(nameQuotes).append(column.getName()).append(nameQuotes).append(", ");
            column.setQuotes(valueQuotes);

            value = column.getQuotedValue();
            if (isOracle && !value.equals("null")) {
                value = oracleValue(column.getType(), value);
            }

            values.append(value).append(", ");
        }
        columns.setLength(columns.length() - 2);
        values.setLength(values.length() - 2);

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
            if (isOracle && !value.equals("null")) {
                value = oracleValue(column.getType(), value);
            }

            values += value + ", ";
        }
        columns = columns.substring(0, columns.length() - 2);
        values = values.substring(0, values.length() - 2);

        String sqlInsert = "INSERT INTO " + nameQuotes + tableName + nameQuotes + " (" + columns + ") VALUES (" + values + ");" + eol;
        return sqlInsert;
    }

    private String oracleValue(int type, String value) {
        if (type == Types.DATE || type == Types.TIMESTAMP) {
            value = "TO_DATE(" + value + ",'YYYY/MM/DD HH24:MI:SS')";

        } else if (type == Types.VARCHAR) {
            value = value.replaceAll("[']", "''");
            value = value.substring(1, value.length() - 1);
            if (value.length() > 1000) {
                value = value.substring(1, value.length() - 1);
                StringBuilder nclob = new StringBuilder();
                while (value.length() > 1000) {
                    nclob.append("to_nclob('").append(value.substring(0, 1000)).append("') || ");
                    value = value.substring(1000);
                }
                if (value.length() > 0) {
                    nclob.append("to_nclob('").append(value).append("')");
                } else {
                    nclob.setLength(nclob.length() - 2);
                }
                value = nclob.toString();
            }
        }
        return value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLInsertFormatter.class);
    }

}
