package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DynamicValueType;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.input.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Source extends AppBase {

    private Converter converter;
    private SourceConfig sourceConfig;
    private DataSource dataSource;
    private DataTable dataTable;

    public Source(Application application, String name, Converter converter, SourceConfig sourceConfig) {
        super(application, name);

        this.converter = converter;
        this.sourceConfig = sourceConfig;

        valid = prepare();
        if (valid) valid = validate();

        log.trace("Source({}) is created", name);
    }

    private boolean prepare() {
        log.trace("Source({}).prepare.", name);
        String dataSourceName = sourceConfig.getDataSource();
        dataSource = application.dataSourceMap.get(dataSourceName);
        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Source({}).validate.", name);

        if (dataSource == null) {
            log.error("datasource({}) is not found, required by Converter({})", sourceConfig.getDataSource(), converter.getName());
            application.hasWarning = true;
            return false;
        }

        return true;
    }

    public boolean buildDataTable() {
        log.trace("Source({}).buildDataTable.", name);

        String query = getQuery();
        if (query == null) {
            return false;
        }

        dataTable = dataSource.getDataTable(sourceConfig.getName(), sourceConfig.getId(), query);
        if (dataTable == null) {
            return false;
        }

        dataTable.setOwner(this);
        return true;
    }

    public String getQuery() {
        log.debug("Source.getQuery");

        String query = sourceConfig.getQuery();
        String returnValue = query;
        String compileResult;

        try {
            for (; true; returnValue = compileResult) {
                compileResult = compileQuery(returnValue);
                if (compileResult == null) {
                    return returnValue;
                }
            }
        } catch (Exception ex) {
            log.error("Source.getQuery. Query with dynamic value is invalid, {}, {}", ex.getMessage(), query);
            return null;
        }

    }

    private String compileQuery(String query) {
        int start = query.indexOf("$(");
        if (start < 0) {
            return null;
        }

        int end = query.indexOf(")", start);
        if (end < 0) {
            end = query.length() - 1;
        }

        String valueMapping = query.substring(start + 2, end);
        log.debug("Source.compileQuery: valueMapping({})", valueMapping);

        int valueTypeIndex = valueMapping.indexOf(":");
        String valueTypeString = valueMapping.substring(0, valueTypeIndex);
        String valueIdentifier = valueMapping.substring(valueTypeIndex + 1);
        log.debug("Source.compileQuery: valueType({}) valueIdentifier({})", valueTypeString, valueIdentifier);

        DynamicValueType valueType = DynamicValueType.valueOf(valueTypeString);
        String replacement = "";
        if (DynamicValueType.TXT.equals(valueType)) {
            replacement = queryFromFile(valueIdentifier);
        } else {
            String[] dataTableMapping = valueMapping.split("[.]");
            replacement = valuesFromDataTable(dataTableMapping[0], dataTableMapping[1]);
        }

        String replaced = query.substring(0, start) + replacement + query.substring(end + 1);
        return replaced;
    }

    private String valuesFromDataTable(String dataTableMapping, String columnName) {
        log.debug("Source.valuesFromDataTable(dataTableMapping:{}, columnName:{})", dataTableMapping, columnName);

        DataTable dataTable = converter.getDataTable(dataTableMapping);
        if (dataTable == null) {
            log.warn("Source.valuesFromDataTable. The specified dataTable({}) is not found.", dataTableMapping);
            return "";
        }

        if (dataTable == null || dataTable.getRowCount() == 0) {
            log.warn("Source.valuesFromDataTable. dataTable({}) is empty.", dataTableMapping);
            return "";
        }

        if (dataTable.getRow(0).getColumn(columnName) == null) {
            log.warn("Source.valuesFromDataTable. The specified column({}) is not found in dataTable({}).", columnName, dataTableMapping);
            return "";
        }

        String value = "";
        for (DataRow row : dataTable.getAllRow()) {
            value += row.getColumn(columnName).getQuotedValue() + ",";
        }

        value = value.substring(0, value.length() - 1);
        log.debug("Source.valuesFromDataTable. return-value={}", value);
        return value;
    }

    private String queryFromFile(String fileName) {

        BufferedReader br = null;
        String content = "";
        try {
            br = new BufferedReader(new FileReader(fileName));
            for (String line; (line = br.readLine()) != null; ) {
                content += line + "\n";
            }

        } catch (FileNotFoundException fx) {
            log.error("SQLSource.queryFromFile. file not found: {}", fx.getMessage());

        } catch (Exception ex) {
            log.error("SQLSource.queryFromFile. has exception: {}", ex);

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("close file {} is failed, {}", fileName, e);
                }
            }
        }

        return content;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Source.class);
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public DataTable getDataTable() {
        if (valid && dataTable == null) {
            valid = buildDataTable();
        }
        return dataTable;
    }
}
