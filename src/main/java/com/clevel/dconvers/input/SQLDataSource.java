package com.clevel.dconvers.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.HashMap;

public class SQLDataSource extends DataSource {

    public SQLDataSource(Application application, String name, DataSourceConfig dataSourceConfig) {
        super(application, name, dataSourceConfig);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLDataSource.class);
    }

    @Override
    public boolean open() {
        // nothing here, open file is in getDataTable function.
        return true;
    }

    @Override
    public DataTable getDataTable(String tableName, String idColumnName, String sqlFileName, HashMap<String, String> queryParamMap) {

        String nameQuote = queryParamMap.get(Property.QUOTES.connectKey(Property.NAME).toUpperCase());
        String valueQuote = queryParamMap.get(Property.QUOTES.connectKey(Property.VALUE).toUpperCase());

        if (nameQuote == null) {
            nameQuote = "";
        }
        if (valueQuote == null) {
            valueQuote = "\"";
        }
        log.debug("SQLDataSource: nameQuote({}) valueQuote({}) queryParamMap({})", nameQuote, valueQuote, queryParamMap);

        DataTable dataTable = new DataTable(application, tableName, idColumnName);
        dataTable.setDataSource(name);
        dataTable.setQuery(sqlFileName);

        DataRow dataRow;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(sqlFileName));
            for (String line; (line = br.readLine()) != null; ) {
                if (!line.startsWith("INSERT INTO")) {
                    continue;
                }

                dataRow = getDataRow(line, dataTable, nameQuote, valueQuote);
                if (dataRow == null) {
                    return null;
                }
                dataTable.addRow(dataRow);
            }

        } catch (FileNotFoundException fx) {
            error("SQLDataSource.getDataTable. file not found: {}", fx.getMessage());
            dataTable = null;

        } catch (Exception ex) {
            error("SQLDataSource.getDataTable. has exception: {}", ex);
            dataTable = null;

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("close file {} is failed, {}", sqlFileName, e);
                }
            }
        }

        return dataTable;
    }

    private DataRow getDataRow(String line, DataTable dataTable, String nameQuote, String valueQuote) {
        int columnStart = line.indexOf('(') + 1;
        int columnEnd = line.indexOf(')', columnStart);
        int valueStart = line.indexOf('(', columnEnd) + 1;
        int valueEnd = line.indexOf(')', valueStart);

        String columnString = line.substring(columnStart, columnEnd).replaceAll(nameQuote, "");
        String valueString = line.substring(valueStart, valueEnd);

        String[] columns = columnString.split(",");
        String[] values = valueString.split(",");

        DataRow dataRow = new DataRow(application, dataTable);
        int length = columns.length;
        int columnType;
        String columnName;
        String value;
        for (int i = 0; i < length; i++) {
            value = values[i].trim();
            columnType = getColumnType(value, valueQuote);
            columnName = columns[i].trim();
            dataRow.putColumn(columnName, application.createDataColumn(columnName, columnType, value.replaceAll(valueQuote, "")));
        }

        return dataRow;
    }

    private int getColumnType(String sqlValue, String valueQuote) {
        if (sqlValue.contains(valueQuote)) {
            // '2018/07/30 21:12:38' or 'string'
            if (sqlValue.indexOf('/') >= 0 || sqlValue.indexOf(':') >= 0 || sqlValue.indexOf('-') >= 0) {
                return Types.DATE;
            }
            return Types.VARCHAR;
        }

        // 2.4566 or 24566
        if (sqlValue.indexOf('.') >= 0) {
            return Types.DECIMAL;
        }
        return Types.INTEGER;
    }

    @Override
    public void close() {
        // nothing here, close file is in getDataTable function.
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("name", name)
                .toString();
    }
}