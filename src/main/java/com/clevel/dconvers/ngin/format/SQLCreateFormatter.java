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

    public SQLCreateFormatter(Application application, String name) {
        super(application, name, true);
        outputType = "sql file";
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        tableName = dataTable.getTableName();
        idColumnName = dataTable.getIdColumnName();
        return null;
    }

    @Override
    protected String format(DataRow row) {
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

            columnString = "`" + columnString + "`" +getColumnTypeString(column);
            columns += columnString + ", ";
        }

        columns = columns.substring(0, columns.length() - 2);
        switch (idColumnType) {
            case Types.INTEGER:
                columnString = "`" + idColumnName + "` int auto_increment primary key, ";
                break;

            case Types.BIGINT:
                columnString = "`" + idColumnName + "` bigint auto_increment primary key, ";
                break;

            default:
                columnString = "`" + idColumnName + "` varchar(255) primary key, ";
        }

        String sqlCreate = "DROP TABLE IF EXISTS `" + tableName + "`;\nCREATE TABLE `" + tableName + "` ( " + columnString + columns + ") COLLATE=utf8_bin;\n";
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

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                return " varchar(255) default 'NULL' null";

            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                return " decimal(19,2) default 'NULL' null";

            case Types.DATE:
            case Types.TIMESTAMP:
                return " datetime default 'NULL' null";

            default:
                return " varchar(255) default 'NULL' null";
        }
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        return "\nBEGIN;\n";
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLCreateFormatter.class);
    }
}
