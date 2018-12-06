package com.clevel.dconvers.ngin.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.AppBase;
import com.clevel.dconvers.ngin.data.*;
import com.clevel.dconvers.ngin.format.*;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.*;

public class DataSource extends AppBase {

    protected DataSourceConfig dataSourceConfig;
    private Connection connection;

    private boolean useInformationSchema;

    public DataSource(Application application, String name, DataSourceConfig dataSourceConfig) {
        super(application, name);

        this.dataSourceConfig = dataSourceConfig;
        useInformationSchema = false;
        valid = open();

        log.trace("DataSource({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataSource.class);
    }

    public boolean open() {
        log.trace("DataSource({}).open.", name);

        String schema = (useInformationSchema ? "information_schema" : dataSourceConfig.getSchema());
        String url = dataSourceConfig.getUrl();
        url = schema.isEmpty() ? url : url + "/" + schema;
        try {

            log.trace("Loading database driver ...");
            Class.forName(dataSourceConfig.getDriver());
            log.trace("Load driver is successful");

            log.trace("Connecting to database({}) ...", name);
            connection = DriverManager.getConnection(url, dataSourceConfig.getUser(), dataSourceConfig.getPassword());
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
            log.warn("DataSource.getRowCount() function will failed when try on the query that use the Stored Procedure call: {}", ex.getMessage());
        }
        return 0;
    }

    public DataTable getDataTable(String tableName, String idColumnName, String query) {
        log.trace("DataSource.getDataTable.");

        DataTable dataTable;
        Statement statement = null;
        CallableStatement callableStatement = null;

        boolean callStoredProcedure = query.startsWith("{");
        ResultSet resultSet;

        try {

            if (callStoredProcedure) {
                log.trace("Open statement...");
                callableStatement = connection.prepareCall(query); // query like this: {call SHOW_SUPPLIERS()}
                if (query.indexOf("?") < 0) {
                    log.debug("Querying by callableStatement(): {}", query);
                    resultSet = callableStatement.executeQuery();
                } else {
                    callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
                    log.debug("Querying by callableStatement(OUT): {}", query);
                    callableStatement.execute();
                    resultSet = (ResultSet) callableStatement.getObject(1);
                }
            } else {
                log.trace("Open statement...");
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                query = query.replaceAll("[\"]", dataSourceConfig.getValueQuotes());
                log.debug("Querying: {}", query);
                resultSet = statement.executeQuery(query);
            }
            ResultSetMetaData metaData = resultSet.getMetaData();

            log.trace("Creating DataTable...");
            dataTable = createDataTable(resultSet, metaData, tableName, idColumnName);
            dataTable.setQuery(query);
            log.debug("DataTable({}) has {} row(s)", tableName, dataTable.getRowCount());

            log.trace("Close statement...");
            resultSet.close();

        } catch (SQLException se) {
            log.error("SQLException: ", se);
            return null;

        } catch (Exception e) {
            log.error("Exception", e);
            return null;

        } finally {
            try {
                if (statement != null) {
                    log.trace("Close statement...");
                    statement.close();
                }
            } catch (SQLException se2) {
            }
        }

        return dataTable;
    }

    private DataTable createDataTable(ResultSet resultSet, ResultSetMetaData metaData, String tableName, String idColumnName) throws Exception {
        DataTable dataTable = new DataTable(tableName, idColumnName);
        application.currentConverter.setCurrentTable(dataTable);
        dataTable.setMetaData(metaData);

        int columnCount = metaData.getColumnCount();
        log.debug("source({}) has {} columns", tableName, columnCount);

        DataRow dataRow;
        DataColumn dataColumn;
        String columnName;
        int columnType;
        java.util.Date dateValue;
        Date date;
        Timestamp timestamp;

        int rowCount = getRowCount(resultSet);
        ProgressBar progressBar;
        if (rowCount > Defaults.PROGRESS_SHOW_KILO_AFTER.getLongValue()) {
            progressBar = new ProgressBar("Build source(" + tableName + ")", rowCount, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, "K", 1000);
        } else {
            progressBar = new ProgressBar("Build source(" + tableName + ")", rowCount, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, " rows", 1);
        }
        progressBar.maxHint(rowCount);

        Object object;
        while (resultSet.next()) {
            progressBar.step();
            dataRow = new DataRow(dataTable);

            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                columnName = metaData.getColumnName(columnIndex);
                columnType = metaData.getColumnType(columnIndex);

                switch (columnType) {
                    case Types.BIGINT:
                    case Types.NUMERIC:
                        object = resultSet.getObject(columnIndex);
                        dataColumn = new DataLong(columnIndex, Types.BIGINT, columnName, object == null ? null : resultSet.getLong(columnIndex));
                        break;

                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.BOOLEAN:
                    case Types.BIT:
                        object = resultSet.getObject(columnIndex);
                        dataColumn = new DataLong(columnIndex, Types.INTEGER, columnName, object == null ? null : resultSet.getLong(columnIndex));
                        break;

                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.NVARCHAR:
                    case Types.NCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.LONGVARCHAR:
                        dataColumn = new DataString(columnIndex, Types.VARCHAR, columnName, resultSet.getString(columnIndex));
                        break;

                    case Types.DECIMAL:
                    case Types.DOUBLE:
                    case Types.FLOAT:
                    case Types.REAL:
                        dataColumn = new DataBigDecimal(columnIndex, Types.DECIMAL, columnName, resultSet.getBigDecimal(columnIndex));
                        break;

                    case Types.DATE:
                        date = resultSet.getDate(columnIndex);
                        if (date == null) {
                            dateValue = null;
                        } else {
                            dateValue = new Date(date.getTime());
                        }
                        dataColumn = new DataDate(columnIndex, Types.DATE, columnName, dateValue);
                        break;

                    case Types.TIMESTAMP:
                        timestamp = resultSet.getTimestamp(columnIndex);
                        if (timestamp == null) {
                            dateValue = null;
                        } else {
                            dateValue = new Date(timestamp.getTime());
                        }
                        dataColumn = new DataDate(columnIndex, Types.DATE, columnName, dateValue);
                        break;

                    default:
                        dataColumn = new DataString(columnIndex, Types.VARCHAR, columnName, resultSet.getObject(columnIndex).toString());
                }

                dataRow.putColumn(columnName, dataColumn);
            } // end of for

            dataTable.addRow(dataRow);
        } // end of while

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

    public int parseType(String columnType) {

        if ("bigint".compareTo(columnType) == 0) {
            return Types.BIGINT;
        } else if ("int".compareTo(columnType) == 0) {
            return Types.INTEGER;
        } else if ("tinyint".compareTo(columnType) == 0) {
            return Types.INTEGER;
        } else if ("bit".compareTo(columnType) == 0) {
            return Types.INTEGER;
        } else if ("decimal".compareTo(columnType) == 0) {
            return Types.DECIMAL;
        } else if ("double".compareTo(columnType) == 0) {
            return Types.DECIMAL;
        } else if ("varchar".compareTo(columnType) == 0) {
            return Types.VARCHAR;
        } else if ("longtext".compareTo(columnType) == 0) {
            return Types.VARCHAR;
        } else if ("text".compareTo(columnType) == 0) {
            return Types.VARCHAR;
        } else if ("timestamp".compareTo(columnType) == 0) {
            return Types.DATE;
        } else if ("datetime".compareTo(columnType) == 0) {
            return Types.DATE;
        }

        log.warn("parseType. found unknown type('{}'), Types.VARCHAR is returned", columnType);
        return Types.VARCHAR;
    }

    public DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    @Override
    public String toString() {
        return dataSourceConfig.toString();
    }

    public void runPre() {
        // TODO Run SQL from file specified in dataSourceConfig.getPre()
    }

    public void runPost() {
        // TODO Run SQL from file specified in dataSourceConfig.getPost()
    }

    private Statement createStatement(String dbmsString) {
        DBMS dbms = DBMS.parse(dbmsString);
        if (dbms == null) {
            return null;
        }
        log.debug("createStatement({})", dbms);

        Statement statement = null;
        try {

            switch (dbms) {
                case SQLSERVER:
                    statement = connection.createStatement();
                    break;

                default:
                    statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            }

        } catch (SQLException se) {
            log.error("SQLException: {}", se.getMessage());

        } catch (Exception ex) {
            log.error("Exception: {}", ex.getMessage());
        }

        return statement;
    }

    public boolean executeUpdate(String sql) {
        Statement statement = null;
        boolean success = true;

        try {
            log.trace("Open statement...");
            statement = createStatement(dataSourceConfig.getDbms());
            if (statement == null) {
                throw new Exception("Create statement for update is failed, sql(" + sql + ")");
            }

            log.debug("DataSource({}).executeUpdate: sql = {}", name, sql);
            int affected = statement.executeUpdate(sql);
            if (affected == 0) {
                log.warn("DataSource({}) no data has changed by last sql!", name);
                application.hasWarning = true;
            }
        } catch (SQLException se) {
            log.error("SQLException: {}", se.getMessage());
            success = false;

        } catch (Exception e) {
            log.error("Exception", e);
            success = false;

        } finally {
            try {
                if (statement != null) {
                    log.trace("Close statement...");
                    statement.close();
                }
            } catch (SQLException se2) {
                // do nothing
            }
        }

        return success;
    }
}