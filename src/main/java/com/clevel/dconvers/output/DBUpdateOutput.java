package com.clevel.dconvers.output;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.DataFormatter;
import com.clevel.dconvers.format.SQLUpdateFormatter;
import com.clevel.dconvers.input.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DBUpdateOutput extends Output {

    private DataSource dataSource;

    public DBUpdateOutput(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        List<String> columnList = outputConfig.getDbUpdateColumnList();
        String tableName = outputConfig.getDbUpdateTable();
        String nameQuotes = outputConfig.getDbUpdateNameQuotes();
        String valueQuotes = outputConfig.getDbUpdateValueQuotes();

        String dataSourceName = outputConfig.getDbUpdateDataSource();
        dataSource = dconvers.getDataSource(dataSourceName.toUpperCase());
        if (dataSource == null) {
            error("DBUpdateOutput: Datasource({}) is not found, required by dbupdate.datasource",dataSourceName);
            return dataFormatterList;
        }
        String dbms = dataSource.getDataSourceConfig().getDbms();

        dataFormatterList.add(new SQLUpdateFormatter(dconvers, tableName, dbms, columnList, nameQuotes, valueQuotes, "\n"));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {

        String dataSourceName = outputConfig.getDbUpdateDataSource();
        dataSource = dconvers.getDataSource(dataSourceName.toUpperCase());
        if (dataSource == null) {
            return null;
        }

        Writer writer = new StringWriter();
        List<String> preSQL = outputConfig.getDbUpdatePreSQL();
        if (preSQL.size() > 0) {
            try {
                for (String sql : preSQL) {
                    writer.write(sql + "\n");
                }
            } catch (IOException e) {
                error("DBUpdateOutput: write the pre-sql failed, {}", e.getMessage());
                return null;
            }
        }

        return writer;
    }

    @Override
    protected boolean closeWriter(OutputConfig outputConfig, DataTable dataTable, Writer writer, boolean success) {

        if (!success) {
            return false;
        }

        List<String> postSQL = outputConfig.getDbUpdatePostSQL();
        if (postSQL.size() > 0) {
            try {
                for (String sql : postSQL) {
                    writer.write(sql + "\n");
                }
            } catch (IOException e) {
                error("DBUpdateOutput: write the post-sql failed, {}", e.getMessage());
                return false;
            }
        }

        String sql = writer.toString();
        if (sql.isEmpty()) {
            return true;
        }

        String[] sqlStatements = sql.split("[;]");
        for (String sqlStatement : sqlStatements) {
            sqlStatement = sqlStatement.trim();
            if (sqlStatement.length() == 0) {
                continue;
            }

            if (dataSource.executeUpdate(sqlStatement)) {
                log.debug("DBUpdateOutput.executeUpdate({}) is success.", sqlStatement);
            } else {
                log.debug("DBUpdateOutput.executeUpdate({}) is failed.", sqlStatement);
                return false;
            }
        }

        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DBUpdateOutput.class);
    }

}
