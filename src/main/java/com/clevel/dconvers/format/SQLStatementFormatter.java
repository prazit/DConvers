package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.input.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLStatementFormatter extends DataFormatter {

    private String columnName;
    private DataSource dataSource;
    private List<String> preSQL;
    private List<String> postSQL;

    private String statement;

    public SQLStatementFormatter(Application application, String name, String columnName, String dataSourceName, List<String> preSQL, List<String> postSQL) {
        super(application, name, true);
        this.columnName = columnName;
        this.preSQL = preSQL;
        this.postSQL = postSQL;
        dataSource = application.getDataSource(dataSourceName);

        statement = "";
        outputType = "DB-Execute";
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        if (dataSource == null) {
            error("DataSource({}) not found, that required to run sql statement.");
        }

        for (String sql : preSQL) {
            sql = sql.replace(';', ' ');
            if (!dataSource.executeUpdate(sql)) {
                return null;
            }
        }

        return null;
    }

    @Override
    public String format(DataRow row) {
        DataColumn dataColumn = row.getColumn(columnName);
        if (dataColumn == null) {
            error("Unknown column({})", columnName);
            return null;
        }

        String value = dataColumn.getValue();
        if (value != null) {
            statement += "\n" + value.trim();
            if (statement.endsWith(";")) {
                statement = statement.replace(';', ' ');
                dataSource.executeUpdate(statement);
                statement = "";
            }
        }

        return null;
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        for (String sql : postSQL) {
            sql = sql.replace(';', ' ');
            if (!dataSource.executeUpdate(sql)) {
                return null;
            }
        }

        return null;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLStatementFormatter.class);
    }

}
