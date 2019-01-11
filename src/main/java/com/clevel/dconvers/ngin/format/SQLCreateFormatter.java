package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.List;

public class SQLCreateFormatter extends DataFormatter {

    private String tableName;
    private String idColumnName;
    private String nameQuotes;
    private boolean needBegin;
    private String eol;

    public SQLCreateFormatter(Application application, String name, String nameQuotes, String eol, boolean needBegin) {
        super(application, name, false);
        tableName = name;
        this.nameQuotes = nameQuotes;
        this.eol = eol;
        this.needBegin = needBegin;
        outputType = "sql file";
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        idColumnName = dataTable.getIdColumnName();
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
                columnString = nameQuotes + idColumnName + nameQuotes + " int auto_increment primary key, ";
                break;

            case Types.BIGINT:
                columnString = nameQuotes + idColumnName + nameQuotes + " bigint auto_increment primary key, ";
                break;

            default:
                columnString = nameQuotes + idColumnName + nameQuotes + " varchar(255) primary key, ";
        }

        String sqlCreate = "DROP TABLE IF EXISTS " + nameQuotes + tableName + nameQuotes + ";" + eol + "CREATE TABLE " + nameQuotes + tableName + nameQuotes + " ( " + columnString + columns + ") COLLATE=utf8_bin;" + eol;
        return sqlCreate;
    }

    private String getColumnTypeString(DataColumn column) {
        int columnType = column.getType();
        switch (columnType) {
            case Types.BIGINT:
            case Types.NUMERIC:
                return " bigint default '0' null";

            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.BOOLEAN:
            case Types.BIT:
                return " int default '0' null";

            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                return " decimal(19,2) null";

            case Types.DATE:
            case Types.TIMESTAMP:
                return " datetime null";

            /*case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:*/
            default:
                return " varchar(255) default 'NULL' null";
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
