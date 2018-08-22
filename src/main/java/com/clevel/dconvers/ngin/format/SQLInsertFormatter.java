package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;

public class SQLInsertFormatter extends DataFormatter {

    public SQLInsertFormatter() {
        super(true);
    }

    @Override
    protected String format(DataRow row) {
        String columns = "";
        String values = "";

        for (DataColumn column : row.getColumnList()) {
            columns += "`" + column.getName() + "`, ";
            values += column.getQuotedValue() + ", ";
        }
        columns = columns.substring(0, columns.length() - 2);
        values = values.substring(0, values.length() - 2);

        String sqlInsert = "INSERT INTO `" + row.getDataTable().getTableName() + "` (" + columns + ") VALUES (" + values + ");\n";
        return sqlInsert;
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        String lines = "";

        for (String sql : dataTable.getPostUpdate()) {
            lines += sql + "\n";
        }
        lines += "COMMIT;\n";

        return lines;
    }
}
