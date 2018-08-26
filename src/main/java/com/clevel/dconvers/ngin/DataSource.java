package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.data.*;
import com.clevel.dconvers.ngin.format.*;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.*;

public class DataSource extends AppBase {

    private DataSourceConfig dataSourceConfig;
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
        try {

            log.trace("Loading database driver ...");
            Class.forName(dataSourceConfig.getDriver());
            log.trace("Load driver is successful");

            log.trace("Connecting to database({}) ...", name);
            connection = DriverManager.getConnection(dataSourceConfig.getUrl() + "/" + schema, dataSourceConfig.getUser(), dataSourceConfig.getPassword());
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

    public DataTable getDataTable(String tableName, String idColumnName, String query) {
        log.trace("DataSource.getDataTable.");
        DataTable dataTable = new DataTable(tableName, idColumnName);
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
            dataTable = createDataTable(resultSet, metaData, tableName, idColumnName);
            dataTable.setQuery(query);
            log.info("DataTable({}) has {} rows", tableName, dataTable.getRowCount());

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

    private DataTable createDataTable(ResultSet resultSet, ResultSetMetaData metaData, String tableName, String idColumnName) throws Exception {
        DataTable dataTable = new DataTable(tableName, idColumnName);
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

    public void generateConverterFile() {

        log.trace("DataSource({}).generateConverterFile", name);
        if (!dataSourceConfig.isGenerateConverterFile()) {
            return;
        }

        close();
        useInformationSchema = true;
        if (!open()) {
            return;
        }

        log.info("DataSource({}).generateConverterFile", name);
        String targetTableName = "targets";
        DataTable tables;
        DataTable columns;
        DataTable targets = new DataTable(targetTableName, "TABLE_NAME");
        DataRow targetRow;
        DataString column;
        DataString nameColumn;
        DataString typeColumn;

        int columnType;
        String tableName;
        String targetKey;
        String columnKey;
        String columnName;
        String columnValue;
        String schema = dataSourceConfig.getSchema();
        String tablesQuery = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = '" + schema + "' ORDER BY TABLE_NAME";
        String query;

        tables = getDataTable("tables", "TABLE_NAME", tablesQuery);
        for (DataRow tableRow : tables.getAllRow()) {
            column = (DataString) tableRow.getColumn("TABLE_NAME");
            tableName = column.getQuotedValue().replaceAll("['\"]", "");
            targetRow = new DataRow(targets);

            targetKey = Property.TARGET.key();
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, tableName));

            targetKey = "#" + Property.TARGET.connectKey(tableName, Property.INDEX);
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, "1"));

            targetKey = Property.TARGET.connectKey(tableName, Property.SOURCE);
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, tableName));

            targetKey = Property.TARGET.connectKey(tableName, Property.TABLE);
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, tableName));

            targetKey = Property.TARGET.connectKey(tableName, Property.CREATE);
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, "false"));

            targetKey = Property.TARGET.connectKey(tableName, Property.INSERT);
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, "true"));

            targetKey = Property.TARGET.connectKey(tableName, Property.MARKDOWN);
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, "true"));

            targetKey = "#" + Property.TARGET.connectKey(tableName, Property.ROW_NUMBER);
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, "1"));

            targetKey = "#" + Property.TARGET.connectKey(tableName, Property.OUTPUT_FILE);
            targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, tableName + ".sql"));

            query = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY FROM COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND TABLE_NAME = '" + tableName + "' ORDER BY COLUMN_KEY DESC, ORDINAL_POSITION ASC";
            columns = getDataTable(tableName + "_columns", "COLUMN_NAME", query);

            log.debug("generateConverterFile. table({}) = {}", tableName, columns);
            for (DataRow columnRow : columns.getAllRow()) {
                columnKey = tableName + ".column";

                nameColumn = (DataString) columnRow.getColumn("COLUMN_NAME");
                typeColumn = (DataString) columnRow.getColumn("DATA_TYPE");
                column = (DataString) columnRow.getColumn("COLUMN_KEY");

                columnName = nameColumn.getValue();
                columnType = parseType(typeColumn.getValue());
                columnValue = defaultValue(columnType);

                if ("PRI".compareTo(column.getValue()) == 0) {
                    targetKey = Property.TARGET.connectKey(tableName, Property.ID);
                    targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, columnName));

                    targetKey = Property.TARGET.connectKey(columnKey + "." + columnName);
                    if (columnType == Types.BIGINT) {
                        targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, SourceColumnType.VAR.getValuePrefix() + SystemVariable.ROWNUMBER.name()));
                    } else {
                        targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, columnValue));
                    }
                } else {
                    targetKey = Property.TARGET.connectKey(columnKey + "." + columnName);
                    targetRow.putColumn(targetKey, application.createDataColumn(targetKey, Types.VARCHAR, columnValue));
                }
            }

            targets.addRow(targetRow);
        }

        close();
        useInformationSchema = false;
        if (!open()) {
            return;
        }


        log.trace("DataSource({}).generateConverterFile.print", name);
        ConverterConfigFileFormatter formatter = new ConverterConfigFileFormatter();
        formatter.setDataSourceName(name);

        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;
        String charset = "UTF-8";
        String outputFile = dataConversionConfigFile.getOutputTargetPath() + "generated-converter.conf";
        Writer writer;

        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), charset);
            log.trace("generate DataTable({}) to File({}) ...", targetTableName, outputFile);
        } catch (Exception e) {
            log.warn("Create output file for '{}' table is failed, {}, generate to System.out instead", targetTableName, e.getMessage());
            application.hasWarning = true;
            return;
        }

        if (writer == null) {
            try {
                writer = new OutputStreamWriter(System.out, charset);
                log.trace("generate DataTable({}) to console ...", targetTableName);
            } catch (Exception e) {
                log.error("System.out is not ready, {}, try again later", e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        java.util.Date date = new java.util.Date();
        int rowCount = targets.getRowCount();
        String header = "#\n"
                + "# Generated by dconvers at " + date.toString() + ".\n"
                + "# This converter file contains " + rowCount + " targets and " + rowCount + " sources from datasource '" + name + "' in " + application.switches.getSource() + "\n"
                + "# Data from: " + tablesQuery + "\n"
                + "#\n";
        try {
            writer.write(header);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        formatter.print(targets, writer);

        try {
            writer.close();
        } catch (Exception e) {
            log.warn("Close file({}) is failed, ", e.getMessage());
            application.hasWarning = true;
        }
    }

    private String defaultValue(int columnType) {
        switch (columnType) {
            case Types.BIGINT:
            case Types.INTEGER:
                return "INT:0";
            case Types.DECIMAL:
                return "DEC:0.0";
            case Types.DATE:
                return "DTT:NULL";
            default: // Types.VARCHAR:
                return "STR:NULL";
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
}