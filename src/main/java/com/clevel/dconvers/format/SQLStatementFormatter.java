package com.clevel.dconvers.format;

import com.clevel.dconvers.DConvers;
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

    public SQLStatementFormatter(DConvers dconvers, String name, String columnName, String dataSourceName, List<String> preSQL, List<String> postSQL) {
        super(dconvers, name, true);
        this.columnName = columnName;
        this.preSQL = preSQL;
        this.postSQL = postSQL;
        dataSource = dconvers.getDataSource(dataSourceName);

        statement = "";
        outputType = "DB-Execute";
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        if (dataSource == null) {
            error("DataSource({}) not found, that required to run sql statement.");
        }

        StringBuilder sqlHistory = new StringBuilder();
        sqlHistory.append("Preprocess: has ").append(preSQL.size()).append(" sql\n");
        if (postSQL.size() == 0) {
            return null;
        }

        for (String sql : preSQL) {
            sql = sql.replace(';', ' ');
            if (!dataSource.executeUpdate(sql)) {
                sqlHistory.append("FAILED - ").append(sql).append("\n");
                return sqlHistory.toString();
            }
            sqlHistory.append("SUCCESS - ").append(sql).append("\n");
        }

        sqlHistory.append("DataTable(").append(dataTable.getName()).append(") has ").append(dataTable.getRowCount()).append(" rows\n");
        return sqlHistory.toString();
    }

    @Override
    public String format(DataRow row) {
        DataColumn dataColumn = row.getColumn(columnName);
        if (dataColumn == null) {
            error("Unknown column({})", columnName);
            return null;
        }

        String value = dataColumn.getValue();
        String sqlHistory = null;
        if (value != null) {
            statement += "\n" + value.trim();
            if (statement.endsWith(";")) {
                statement = statement.replace(';', ' ').trim();
                boolean success = dataSource.executeUpdate(statement);
                if (success) {
                    sqlHistory = "SUCCESS - " + statement + "\n";
                } else {
                    sqlHistory = "FAILED - " + statement + "\n";
                }
                statement = "";
            }
        }

        return sqlHistory;
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        StringBuilder sqlHistory = new StringBuilder();
        sqlHistory.append("Postprocess: has ").append(postSQL.size()).append(" rows\n");
        if (postSQL.size() == 0) {
            return null;
        }

        for (String sql : postSQL) {
            sql = sql.replace(';', ' ');
            if (!dataSource.executeUpdate(sql)) {
                sqlHistory.append("FAILED - ").append(sql).append("\n");
                return sqlHistory.toString();
            }
            sqlHistory.append("SUCCESS - ").append(sql).append("\n");
        }

        return sqlHistory.toString();
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLStatementFormatter.class);
    }

}
