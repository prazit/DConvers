package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.input.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        dataSource = application.getDataSource(dataSourceName.toUpperCase());
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
