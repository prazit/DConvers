package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.input.DBMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;

public class SQLCreateFormatter extends DataFormatter {

    private String tableName;
    private String idColumnName;
    private DBMS dbms;
    private String nameQuotes;
    private boolean needBegin;
    private String eol;

    private HashMap<String,Boolean> bigStringOrNot;

    private String endOfCreate;

    private String columnTypeBigInt;
    private String columnTypeInteger;
    private String columnTypeDecimal;
    private String columnTypeDate;
    private String columnTypeString;
    private String columnTypeBigString;

    private String pkTypeBigInt;
    private String pkTypeInteger;
    private String pkTypeString;

    public SQLCreateFormatter(Application application, String name, String dbms, String nameQuotes, String eol, boolean needBegin) {
        super(application, name, false);
        tableName = name;
        this.dbms = DBMS.parse(dbms);
        this.nameQuotes = nameQuotes;
        this.eol = eol;
        this.needBegin = needBegin;
        outputType = "sql file";

        bigStringOrNot = new HashMap<>();

        if (DBMS.MYSQL.equals(this.dbms)) {
            columnTypeBigInt = " bigint DEFAULT NULL";
            columnTypeInteger = " int DEFAULT NULL";
            columnTypeDecimal = " decimal(19,2) DEFAULT NULL";
            columnTypeDate = " datetime DEFAULT NULL";
            columnTypeString = " varchar(255) DEFAULT NULL";
            columnTypeBigString = " text DEFAULT NULL";

            pkTypeBigInt = " bigint NOT NULL AUTO_INCREMENT";
            pkTypeInteger = " int NOT NULL AUTO_INCREMENT";
            pkTypeString = " varchar(255) NOT NULL";

            endOfCreate = " ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;";

        } else {
            columnTypeBigInt = " bigint default '0' null";
            columnTypeInteger = " int default '0' null";
            columnTypeDecimal = " decimal(19,2) null";
            columnTypeDate = " datetime null";
            columnTypeString = " varchar(255) default 'NULL' null";
            columnTypeBigString = " varchar(1000) default 'NULL' null";

            pkTypeBigInt = " bigint auto_increment primary key";
            pkTypeInteger = "  int auto_increment primary key";
            pkTypeString = " varchar(255) primary key";

            endOfCreate = ";";
        }
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        idColumnName = dataTable.getIdColumnName();

        if (dataTable.getRowCount() == 0) {
            return null;
        }

        DataRow firstRow = dataTable.getRow(0);
        for (DataColumn dataColumn : firstRow.getColumnList()) {
            if (dataColumn.getType() == Types.VARCHAR) {
                bigStringOrNot.put(dataColumn.getName().toUpperCase(), false);
            }
        }

        HashMap<String,Boolean> readyColumns = new HashMap<>();
        int normalStringLength = 255;
        DataString dataColumn;
        String value;
        String key;
        for (DataRow dataRow : dataTable.getRowList()) {
            for (String columnName : bigStringOrNot.keySet()) {
                key = columnName.toUpperCase();

                dataColumn = (DataString) dataRow.getColumn(columnName);
                value = dataColumn.getValue();

                if (value != null && value.length() > normalStringLength) {
                    readyColumns.put(key, true);
                    bigStringOrNot.remove(key);
                    break;
                }
            }
        }
        bigStringOrNot.putAll(readyColumns);

        return null;
    }

    @Override
    public String format(DataRow row) {
        /*
            create table source_to_target
            (
                id bigint auto_increment primary key,
                action varchar(255) default 'NULL' null,
                action_date datetime default 'NULL' null,
                action_flag int default '0' null,
                action_amount decimal(19,2) default 'NULL' null,
            )
            collate=utf8_bin
            ;
        */

        List<DataColumn> columnList = row.getColumnList();
        if (columnList.size() == 0) {
            return "";
        }

        String columns = "";
        String columnString;
        int idColumnType = Types.BIGINT;

        for (DataColumn column : columnList) {
            columnString = column.getName();
            if (columnString.compareTo(idColumnName) == 0) {
                idColumnType = column.getType();
                continue;
            }

            columnString = nameQuotes + columnString + nameQuotes + getColumnTypeString(column);
            columns += columnString + ", ";
        }

        columns = columns.substring(0, columns.length() - 2);
        switch (idColumnType) {
            case Types.INTEGER:
                columnString = nameQuotes + idColumnName + nameQuotes + pkTypeInteger + ", ";
                break;

            case Types.BIGINT:
                columnString = nameQuotes + idColumnName + nameQuotes + pkTypeBigInt + ", ";
                break;

            default:
                columnString = nameQuotes + idColumnName + nameQuotes + pkTypeString + ", ";
        }

        String sqlCreate = "DROP TABLE IF EXISTS " + nameQuotes + tableName + nameQuotes + ";" + eol
                + "CREATE TABLE " + nameQuotes + tableName + nameQuotes + " ( " + columnString + columns
                + (DBMS.MYSQL.equals(dbms) ? ", PRIMARY KEY (" + nameQuotes + idColumnName + nameQuotes + "), UNIQUE KEY " + nameQuotes + idColumnName + nameQuotes + " (" + nameQuotes + idColumnName + nameQuotes + ")" : "")
                + ") " + endOfCreate + eol;

        return sqlCreate;
    }

    private String getColumnTypeString(DataColumn column) {
        int columnType = column.getType();
        switch (columnType) {
            case Types.BIGINT:
            case Types.NUMERIC:
                return columnTypeBigInt;

            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.BOOLEAN:
            case Types.BIT:
                return columnTypeInteger;

            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                return columnTypeDecimal;

            case Types.DATE:
            case Types.TIMESTAMP:
                return columnTypeDate;

            /*case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:*/
            default:
                String name = column.getName().toUpperCase();
                if (bigStringOrNot.get(name)) {
                    return columnTypeBigString;
                }
                if (name.contains("REMARK")) {
                    return columnTypeBigString;
                }
                return columnTypeString;
        }
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        if (needBegin) {
            return eol + "BEGIN;" + eol;
        }
        return null;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLCreateFormatter.class);
    }
}
