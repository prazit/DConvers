package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;

import java.sql.Types;

public class SQLCreateFormatter extends DataFormatter {

    private String tableName;
    private String idColumnName;

    public SQLCreateFormatter() {
        super(false);
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

        String columns = "";
        String columnString;

        for (DataColumn column : row.getColumnList()) {
            columnString = column.getName();
            if (columnString.compareTo(idColumnName) == 0) {
                continue;
            }

            columnString += getColumnTypeString(column);
            columns += "`" + columnString + "`, ";
        }
        columns = columns.substring(0, columns.length() - 2);
        columnString = idColumnName + " bigint auto_increment primary key, ";

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

}
