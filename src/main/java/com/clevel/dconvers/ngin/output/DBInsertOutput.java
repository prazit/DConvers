package com.clevel.dconvers.ngin.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.DataFormatter;
import com.clevel.dconvers.ngin.format.SQLInsertFormatter;
import com.clevel.dconvers.ngin.input.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DBInsertOutput extends Output {

    private DataSource dataSource;

    public DBInsertOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        String tableName = outputConfig.getDbInsertTable();
        String nameQuotes = outputConfig.getDbInsertNameQuotes();
        String valueQuotes = outputConfig.getDbInsertValueQuotes();

        dataFormatterList.add(new SQLInsertFormatter(application, tableName, nameQuotes, valueQuotes, "\n"));

        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {

        String dataSourceName = outputConfig.getDbInsertDataSource();
        dataSource = application.dataSourceMap.get(dataSourceName.toUpperCase());
        if (dataSource == null) {
            return null;
        }

        Writer writer = new StringWriter();
        List<String> preSQL = outputConfig.getDbInsertPreSQL();
        if (preSQL.size() > 0) {
            try {
                for (String sql : preSQL) {
                    writer.write(sql + "\n");
                }
            } catch (IOException e) {
                error("DBInsertOutput: write the pre-sql failed, {}", e.getMessage());
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

        List<String> postSQL = outputConfig.getDbInsertPostSQL();
        if (postSQL.size() > 0) {
            try {
                for (String sql : postSQL) {
                    writer.write(sql + "\n");
                }
            } catch (IOException e) {
                error("DBInsertOutput: write the post-sql failed, {}", e.getMessage());
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
                log.debug("DBInsertOutput.executeInsert is success.");
            } else {
                log.debug("DBInsertOutput.executeInsert is failed.");
                return false;
            }
        }

        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DBInsertOutput.class);
    }

}
