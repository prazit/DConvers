package com.clevel.dconvers.input;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.data.*;
import com.clevel.dconvers.ngin.TimeTrackerKey;
import com.clevel.dconvers.ngin.UtilBase;
import javafx.util.Pair;
import me.tongfei.progressbar.ProgressBar;
import oracle.jdbc.OracleTypes;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class DataSource extends UtilBase {

    protected DataSourceConfig dataSourceConfig;
    private Connection connection;

    private boolean useInformationSchema;

    public DataSource(DConvers dconvers, String name, DataSourceConfig dataSourceConfig) {
        super(dconvers, name);

        this.dataSourceConfig = dataSourceConfig;
        useInformationSchema = false;
        valid = open();

        log.trace("DataSource({}) is created", name);
    }

    /**
     * asLib: predefined datasource.
     * <p>
     * usage: DataSource dataSource = new DataSource(dconvers, "datasource-name", connection);
     * and now the DataSource is ready to use, assign "datasource-name" to the table in converter configuration file before call dconvers.start().
     */
    public DataSource(DConvers dconvers, String name, Connection connection) {
        super(dconvers, name);

        this.dataSourceConfig = new DataSourceConfig(dconvers, connection);
        this.connection = connection;

        useInformationSchema = false;
        valid = true;

        log.trace("DataSource({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataSource.class);
    }

    public boolean open() {
        log.trace("DataSource({}).open.", name);

        connection = getExistingConnection();
        if (connection != null) {
            log.info("Use shared connection for datasource({})", name);
            return true;
        }

        String schema = (useInformationSchema ? "information_schema" : dataSourceConfig.getSchema());
        String url = dataSourceConfig.getUrl();
        url = schema.isEmpty() ? url : url + "/" + schema;
        try {

            log.trace("Loading database driver ...");
            Class.forName(dataSourceConfig.getDriver());
            log.trace("Load driver is successful");

            Properties properties = new Properties();
            List<Pair<String, String>> propList = dataSourceConfig.getPropList();
            if (propList.size() > 0) {
                for (Pair<String, String> propertyPair : propList) {
                    properties.put(propertyPair.getKey(), propertyPair.getValue());
                }
                log.debug("Connection properties = {}", propList);
            }

            properties.put("user", dataSourceConfig.getUser());
            properties.put("password", dataSourceConfig.getPassword());

            log.trace("Connecting to datasource({})[{}]", name, toString());
            connection = DriverManager.getConnection(url, properties);
            log.info("Connected to datasource({})", name);

            return true;

        } catch (SQLException se) {
            log.info("Can't connect to datasource({}), see error.log for detailed", name);
            error("Connection to datasource({}) is failed! [{}]", name, toString());
            log.debug("SQLException = {}", se);

        } catch (Exception e) {
            log.info("Can't connect to datasource({}), see error.log for detailed", name);
            error("Load driver of datasource({}) is failed! [{}]", name, toString());
            log.debug("Exception = {}", e);
        }

        return false;
    }

    private Connection getExistingConnection() {
        DataSource dataSource = dconvers.getDataSource(dataSourceConfig);
        if (dataSource == null) {
            return null;
        }
        log.debug("getExistingConnection found existing datasource");
        return dataSource.connection;
    }

    private int getRowCount(ResultSet resultSet) {
        try {
            resultSet.last();
            int size = resultSet.getRow();
            resultSet.beforeFirst();
            //log.debug("getRowCount.size = {}", size);
            return size;
        } catch (Exception ex) {
            log.debug("Exception has ouccured in DataSource.getRowCount() but this is normally for the Stored Procedure call. ");
        }
        return 0;
    }

    public DataTable getDataTable(String tableName, String idColumnName, String query, HashMap<String, String> queryParamMap) {
        if (!isValid()) {
            log.info("DataSource({}).getDataTable return null for invalid datasource.", name);
            return null;
        }

        if (query == null || query.trim().isEmpty()) {
            error("Empty query is not allowed for tableName({}) idColumnName({}) in converter({})", tableName, idColumnName, dconvers.currentConverter.getName());
            return null;
        }

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
                if (!query.contains("?")) {
                    log.debug("Querying by callableStatement(): {}", query);
                    resultSet = callableStatement.executeQuery();
                } else {
                    callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
                    log.debug("Querying by callableStatement(OUT): {}", query);
                    dconvers.timeTracker.start(TimeTrackerKey.DATASOURCE, "data querying for source(" + tableName + ")");
                    callableStatement.execute();
                    resultSet = (ResultSet) callableStatement.getObject(1);
                    dconvers.timeTracker.stop(TimeTrackerKey.DATASOURCE);
                }
            } else {
                log.trace("Open statement...");
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                query = query.replaceAll("[\"]", dataSourceConfig.getValueQuotes());
                //log.debug("Querying: {}", query);
                dconvers.timeTracker.start(TimeTrackerKey.DATASOURCE, "data querying for source(" + tableName + ")");
                resultSet = statement.executeQuery(query);
                dconvers.timeTracker.stop(TimeTrackerKey.DATASOURCE);
                log.trace("execute query is successful");
            }

            if (resultSet != null) {
                ResultSetMetaData metaData = resultSet.getMetaData();

                log.trace("Creating DataTable...");
                dataTable = createDataTable(resultSet, metaData, tableName, idColumnName);
                dataTable.setDataSource(name);
                dataTable.setQuery(query);
                log.debug("DataTable({}) has {} row(s)", tableName, dataTable.getRowCount());

                log.trace("Close statement...");
                resultSet.close();
            } else {
                dataTable = createDataTable(tableName, idColumnName, name, query);
                log.debug("DataTable({}) has 0 row", tableName);
            }

        } catch (SQLException se) {
            dataTable = createDataTable(tableName, idColumnName, name, query);
            error("DataTable({}) has 0 row due to SQLException: {}", tableName, se.getMessage());

        } catch (Exception e) {
            dataTable = createDataTable(tableName, idColumnName, name, query);
            error("DataTable({}) has 0 row due to Exception: {}", tableName, e);

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

    private DataTable createDataTable(String tableName, String idColumnName, String dataSourceName, String query) {
        DataTable dataTable = new DataTable(dconvers, tableName, idColumnName);
        dconvers.currentConverter.setCurrentTable(dataTable);

        dataTable.setDataSource(dataSourceName);
        dataTable.setQuery(query);
        return dataTable;
    }

    private DataTable createDataTable(ResultSet resultSet, ResultSetMetaData metaData, String tableName, String idColumnName) throws Exception {
        DataTable dataTable = new DataTable(dconvers, tableName, idColumnName);
        dconvers.currentConverter.setCurrentTable(dataTable);

        DataTable metaDataTable = new DataTable(dconvers, tableName + "_meta_data", "columnName");
        dataTable.setMetaData(metaDataTable);

        int columnCount = metaData.getColumnCount();
        log.debug("source({}) has {} columns", tableName, columnCount);

        DataRow metaDataRow;
        String metaDataColumnName;
        boolean needMetaData = true;

        DataRow dataRow;
        DataColumn dataColumn;
        String columnLabel;
        int columnType;
        java.util.Date dateValue;
        Date date;
        Timestamp timestamp;

        int rowCount = getRowCount(resultSet);
        ProgressBar progressBar = getProgressBar("Build source(" + tableName + ")", (rowCount == 0 ? 100 : rowCount));

        Object object;
        while (resultSet.next()) {
            progressBar.step();
            dataRow = new DataRow(dconvers, dataTable);

            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                columnLabel = metaData.getColumnLabel(columnIndex);
                columnType = metaData.getColumnType(columnIndex);

                switch (columnType) {
                    case Types.BIGINT:
                        object = resultSet.getObject(columnIndex);
                        dataColumn = new DataLong(dconvers, columnIndex, Types.BIGINT, columnLabel, object == null ? null : resultSet.getLong(columnIndex));
                        break;

                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.BOOLEAN:
                    case Types.BIT:
                        object = resultSet.getObject(columnIndex);
                        dataColumn = new DataLong(dconvers, columnIndex, Types.INTEGER, columnLabel, object == null ? null : resultSet.getLong(columnIndex));
                        break;

                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.NVARCHAR:
                    case Types.NCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.LONGVARCHAR:
                        dataColumn = new DataString(dconvers, columnIndex, Types.VARCHAR, columnLabel, resultSet.getString(columnIndex));
                        break;

                    case Types.CLOB:
                    case Types.NCLOB:
                        dataColumn = new DataString(dconvers, columnIndex, Types.VARCHAR, columnLabel, clobToString(resultSet.getClob(columnIndex)));
                        break;

                    case Types.DECIMAL:
                    case Types.DOUBLE:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.NUMERIC:
                        dataColumn = new DataBigDecimal(dconvers, columnIndex, Types.DECIMAL, columnLabel, resultSet.getBigDecimal(columnIndex));
                        break;

                    case Types.DATE:
                        date = resultSet.getDate(columnIndex);
                        if (date == null) {
                            dateValue = null;
                        } else {
                            dateValue = new Date(date.getTime());
                        }
                        dataColumn = new DataDate(dconvers, columnIndex, Types.DATE, columnLabel, dateValue);
                        break;

                    case Types.TIMESTAMP:
                        timestamp = resultSet.getTimestamp(columnIndex);
                        if (timestamp == null) {
                            dateValue = null;
                        } else {
                            dateValue = new Date(timestamp.getTime());
                        }
                        dataColumn = new DataDate(dconvers, columnIndex, Types.DATE, columnLabel, dateValue);
                        break;

                    default:
                        object = resultSet.getObject(columnIndex);
                        dataColumn = new DataString(dconvers, columnIndex, Types.VARCHAR, columnLabel, object == null ? null : object.toString());
                }
                dataRow.putColumn(columnLabel, dataColumn);

                if (needMetaData) {
                    metaDataRow = new DataRow(dconvers, dataTable);

                    metaDataColumnName = "ColumnName";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.VARCHAR, metaData.getColumnName(columnIndex)));

                    metaDataColumnName = "ColumnLabel";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.VARCHAR, columnLabel));

                    metaDataColumnName = "ColumnClassName";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.VARCHAR, metaData.getColumnClassName(columnIndex)));

                    metaDataColumnName = "ColumnType";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.INTEGER, String.valueOf(columnType)));

                    metaDataColumnName = "ColumnTypeName";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.VARCHAR, metaData.getColumnTypeName(columnIndex)));

                    metaDataColumnName = "Precision";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.INTEGER, String.valueOf(metaData.getPrecision(columnIndex))));

                    metaDataColumnName = "Scale";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.INTEGER, String.valueOf(metaData.getScale(columnIndex))));

                    metaDataColumnName = "ColumnDisplaySize";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.INTEGER, String.valueOf(metaData.getColumnDisplaySize(columnIndex))));

                    metaDataColumnName = "SchemaName";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.VARCHAR, metaData.getSchemaName(columnIndex)));

                    metaDataColumnName = "CatalogName";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.VARCHAR, metaData.getCatalogName(columnIndex)));

                    metaDataColumnName = "TableName";
                    metaDataRow.putColumn(metaDataColumnName, dconvers.createDataColumn(metaDataColumnName, Types.VARCHAR, metaData.getTableName(columnIndex)));

                    metaDataTable.addRow(metaDataRow);
                }
            } // end of for

            dataTable.addRow(dataRow);
            needMetaData = false;
            if (dataTable.getRowCount() == rowCount) {
                progressBar.stepTo(10);
            }
        } // end of while

        progressBar.close();

        if (log.isDebugEnabled()) {
            log.debug("createDataTable({}). has {} rows, firstRow is {}", tableName, dataTable.getRowCount(), dataTable.getRow(0));
        }

        return dataTable;
    }

    private String clobToString(Clob clob) {
        if (clob == null) {
            return null;
        }

        try {
            return clob.getSubString(1L, (int) clob.length());
        } catch (Exception ex) {
            String errMessage = ex.getMessage();
            error("clobToString is failed, {}", errMessage);
            return "CLOB-ERROR(" + errMessage + ")";
        }
    }

    public void close() {
        log.trace("DataSource({}).close.", name);

        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException se) {
            log.warn("Disconnect is failed, already ignored!, {}", se.getMessage());
        } finally {
            connection = null;
            log.info("Disconnected from datasource({})", name);
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
            error("SQLException: {}", se.getMessage());

        } catch (Exception ex) {
            error("Exception: {}", ex.getMessage());
        }

        return statement;
    }

    public boolean executeUpdate(String sql) {
        Statement statement = null;
        boolean success = true;

        try {
            //log.trace("Open statement...");
            statement = createStatement(dataSourceConfig.getDbms());
            if (statement == null) {
                throw new Exception("Create statement for update is failed, sql(" + sql + ")");
            }

            //log.debug("DataSource({}).executeUpdate: sql = {}", name, sql);
            int affected = statement.executeUpdate(sql);
            if (affected == 0) {
                log.debug("DataSource({}).executeUpdate: no data has changed by this sql={}", name, sql);
            }
        } catch (SQLException se) {
            error("SQLException: {}", se.getMessage());
            success = false;

        } catch (Exception e) {
            error("Exception", e);
            success = false;

        } finally {
            try {
                if (statement != null) {
                    //log.trace("Close statement...");
                    statement.close();
                }
            } catch (SQLException se2) {
                // do nothing
            }
        }

        return success;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("name", name)
                .append("dbms", dataSourceConfig.getDbms())
                .append("driver", dataSourceConfig.getDriver())
                .append("url", dataSourceConfig.getUrl())
                .append("schema", dataSourceConfig.getSchema())
                .append("user", dataSourceConfig.getUser())
                .toString()
                .replace('=', ':');
    }
}