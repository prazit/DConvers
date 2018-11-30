package com.clevel.dconvers.ngin.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;

public class MarkdownDataSource extends DataSource {

    private boolean skipFirstColumn;

    public MarkdownDataSource(Application application, String name, DataSourceConfig dataSourceConfig) {
        super(application, name, dataSourceConfig);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(MarkdownDataSource.class);
    }

    @Override
    public boolean open() {
        // nothing here, open file is in getDataTable function.
        return true;
    }

    @Override
    public DataTable getDataTable(String tableName, String idColumnName, String markdownFileName) {

        DataTable dataTable = new DataTable(tableName, idColumnName);
        dataTable.setQuery(markdownFileName);

        DataRow dataRow;
        String[] columnNames = null;
        int[] columnTypes = null;
        boolean isStarted = false;
        int lineCount = 0;
        BufferedReader br = null;

        skipFirstColumn = false;
        try {
            br = new BufferedReader(new FileReader(markdownFileName));
            for (String line; (line = br.readLine()) != null; ) {
                lineCount++;

                if (!line.startsWith("|")) {
                    if (isStarted) {
                        // break when go out of markdown-table
                        log.debug("end of markdown table at line({})", lineCount);
                        break;
                    }
                    continue;
                }

                if (!isStarted) {
                    columnNames = getHeaders(line);
                    isStarted = true;
                    continue;
                }

                if (columnTypes == null) {
                    columnTypes = getColumnType(line);

                } else {
                    dataRow = getDataRow(line, columnNames, columnTypes, dataTable);
                    if (dataRow == null) {
                        return null;
                    }
                    dataTable.addRow(dataRow);
                }

            }
        } catch (FileNotFoundException fx) {
            log.error("SQLSource.getDataTable. file not found: {}", fx.getMessage());
            dataTable = null;

        } catch (Exception ex) {
            log.error("SQLSource.getDataTable. has exception: {}", ex);
            dataTable = null;

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("close file {} is failed, {}", markdownFileName, e);
                }
            }
        }

        return dataTable;
    }

    private String[] splitColumns(String line) {
        String[] columns = line.split("[|]");
        int startColumn = skipFirstColumn ? 2 : 1;
        return Arrays.copyOfRange(columns, startColumn, columns.length);
    }

    private String[] getHeaders(String line) {
        String[] columns = splitColumns(line);
        log.debug("header columns = {}", columns);

        if (columns.length > 0 && columns[0].trim().equals("No.")) {
            skipFirstColumn = true;
            return Arrays.copyOfRange(columns, 1, columns.length);
        }
        return columns;
    }

    private int[] getColumnType(String line) {
        String[] columns = splitColumns(line);
        log.debug("separator columns = {}", columns);

        int[] types = new int[columns.length];
        int index = -1;

        for (String column : columns) {
            index++;
            if (column.startsWith(":")) {
                if (column.endsWith(":")) {
                    // :----:
                    types[index] = Types.DATE;
                } else {
                    // :----
                    types[index] = Types.VARCHAR;
                }
            } else {
                if (column.endsWith(":")) {
                    // ----:
                    types[index] = Types.INTEGER; // this type need to detect for Types.DECIMAL again at the detail record.
                } else {
                    // ----
                    types[index] = Types.VARCHAR;
                }
            }
        }

        return types;
    }

    private DataRow getDataRow(String line, String[] columnNames, int[] columnTypes, DataTable dataTable) {
        String[] columns = splitColumns(line);
        log.debug("separator columns = {}", columns);

        DataRow dataRow = new DataRow(dataTable);
        int index = -1;
        String columnName;
        int columnType;

        for (String column : columns) {
            index++;
            columnName = columnNames[index];
            columnType = columnTypes[index];

            dataRow.putColumn(columnName, application.createDataColumn(columnName.trim(), columnType, column.trim()));
        }

        return dataRow;
    }

    @Override
    public void close() {
        // nothing here, close file is in getDataTable function.
    }

}