package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLInsertFormatter extends DataFormatter {

    public SQLInsertFormatter(Application application, String name) {
        super(application, name, true);

        outputType = "sql file";
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        return "SET FOREIGN_KEY_CHECKS = 0;\n";
    }

    @Override
    protected String format(DataRow row) {
        String columns = "";
        String values = "";

        for (DataColumn column : row.getColumnList()) {
            columns += "`" + column.getName() + "`, ";
            values += column.getQuotedValue().replaceAll("\r\n|\n\r|\n", "\\\\n") + ", ";
        }
        columns = columns.substring(0, columns.length() - 2);
        values = values.substring(0, values.length() - 2);

        String sqlInsert = "INSERT INTO `" + row.getDataTable().getTableName() + "` (" + columns + ") VALUES (" + values + ");\n";
        return sqlInsert;
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        String lines = "";
        String terminated = ";";

        for (String sql : dataTable.getPostUpdate()) {
            if (!sql.endsWith(terminated)) {
                lines += sql + terminated + "\n";
            } else {
                lines += sql + "\n";
            }
        }
        lines += "COMMIT;\n";

        return lines;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLInsertFormatter.class);
    }
}
