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
            error("datasource({}) is not found, required by Converter({})", sourceConfig.getDataSource(), converter.getName());
            application.hasWarning = true;
            return false;
        }

        return true;
    }

    public boolean buildDataTable() {
        log.trace("Source({}).buildDataTable.", name);

        String query = sourceConfig.getQuery();
        if (query == null) {
            return false;
        }

        dataTable = dataSource.getDataTable(sourceConfig.getName(), sourceConfig.getId(), query);
        if (dataTable == null) {
            return false;
        }
        log.info("SRC:{} has {} row(s)", name, dataTable.getRowCount());

        dataTable.setOwner(this);
        return true;
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
