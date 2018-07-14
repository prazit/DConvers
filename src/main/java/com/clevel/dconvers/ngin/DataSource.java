package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.conf.SourceConfig;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;

import static jdk.nashorn.internal.runtime.JSType.isString;

public class DataSource extends AppBase {

    private DataSourceConfig dataSourceConfig;
    private Connection connection;

    public DataSource(Application application, String name, DataSourceConfig dataSourceConfig) {
        super(application, name);

        this.dataSourceConfig = dataSourceConfig;
        valid = open();

        log.trace("DataSource({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataSource.class);
    }

    public boolean open() {
        log.trace("DataSource({}).open.", name);

        try {

            log.trace("Loading database driver ...");
            Class.forName(dataSourceConfig.getDriver());
            log.trace("Load driver is successful");

            log.trace("Connecting to database({}) ...", name);
            connection = DriverManager.getConnection(dataSourceConfig.getUrl() + "/" + dataSourceConfig.getSchema(), dataSourceConfig.getUser(), dataSourceConfig.getPassword());
            log.info("Connected to database({})", name);

            return true;

        } catch (SQLException se) {
            log.error("Connection is failed");
            log.debug("SQLException = {}", se);
        } catch (Exception e) {
            log.error("Load driver is failed");
            log.debug("Exception = {}", e);
        }

        return false;
    }

    private int getRowCount(ResultSet resultSet) {
        try {
            resultSet.last();
            int size = resultSet.getRow();
            resultSet.beforeFirst();
            log.debug("getRowCount.size = {}", size);
            return size;
        } catch (Exception ex) {
            log.error("getRowCount.error: ", ex);
        }
        return 0;
    }

    public DataTable getDataTable(String tableName, String query) {
        log.trace("DataSource.getDataTable.");
        DataTable dataTable = new DataTable(tableName);
        Statement statement = null;

        try {
            log.trace("Open statement...");
            //statement = connection.createStatement();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            log.debug("Querying: {}", query);
            log.info("Loading data({})...", tableName);
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();

            log.trace("Creating DataTable...");
            dataTable = createDataTable(resultSet, metaData, tableName);
            log.info("DataTable({}) has {} row(s)", tableName, dataTable.getRowCount());

            log.trace("Close statement...");
            resultSet.close();
            statement.close();

        } catch (SQLException se) {
            log.error("SQLException: ", se);

        } catch (Exception e) {
            log.error("Exception", e);

        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException se2) {
            }
        }

        return dataTable;
    }

    private DataTable createDataTable(ResultSet resultSet, ResultSetMetaData metaData, String tableName) throws Exception {
        DataTable dataTable = new DataTable(tableName);
        dataTable.setMetaData(metaData);

        int columnCount = metaData.getColumnCount();
        DataRow dataRow;
        DataColumn dataColumn;
        String columnName;
        int columnType;

        int rowCount = getRowCount(resultSet);
        ProgressBar progressBar;
        if (rowCount > 3000) {
            progressBar = new ProgressBar("Build source(" + tableName + ")", rowCount, 500, System.out, ProgressBarStyle.ASCII, "K", 1000);
        } else {
            progressBar = new ProgressBar("Build source(" + tableName + ")", rowCount, 500, System.out, ProgressBarStyle.ASCII, "", 1);
        }
        progressBar.maxHint(rowCount);

        while (resultSet.next()) {
            progressBar.step();
            dataRow = new DataRow(dataTable);

            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                columnName = metaData.getColumnName(columnIndex);
                columnType = metaData.getColumnType(columnIndex);

                switch (columnType) {
                    case Types.BIGINT:
                    case Types.BIT:
                    case Types.BOOLEAN:
                    case Types.INTEGER:
                    case Types.NUMERIC:
                    case Types.SMALLINT:
                        dataColumn = new DataLong(columnIndex, columnType, columnName, resultSet.getLong(columnIndex));
                        break;

                    case Types.DECIMAL:
                    case Types.DOUBLE:
                    case Types.FLOAT:
                    case Types.REAL:
                        dataColumn = new DataBigDecimal(columnIndex, columnType, columnName, resultSet.getBigDecimal(columnIndex));
                        break;

                    case Types.CHAR:
                    case Types.LONGNVARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                    case Types.VARCHAR:
                        dataColumn = new DataString(columnIndex, columnType, columnName, resultSet.getString(columnIndex));
                        break;

                    case Types.DATE:
                    case Types.TIMESTAMP:
                        dataColumn = new DataDate(columnIndex, columnType, columnName, resultSet.getDate(columnIndex));
                        break;

                    default:
                        dataColumn = new DataString(columnIndex, columnType, columnName, resultSet.getObject(columnIndex).toString());
                }

                dataRow.putColumn(columnName, dataColumn);
            } // end for

            dataTable.addRow(dataRow);
        } // end while

        progressBar.close();

        if (log.isDebugEnabled() && rowCount > 0) {
            log.debug("createDataTable({}). has {} rows, firstRow is {}", tableName, rowCount, dataTable.getRow(0));
        }

        return dataTable;
    }

    public void close() {
        log.trace("DataSource({}).close.", name);

        if (connection == null) {
            return;
        }

        try {
            connection.close();
            connection = null;
            log.info("Disconnected from database({})", name);
        } catch (SQLException se) {
            log.error("disconnect is failed");
            log.debug("SQLException = {}", se);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    @Override
    public String toString() {
        return dataSourceConfig.toString();
    }
}