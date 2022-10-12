package com.clevel.dconvers.ngin;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.input.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Source extends AppBase {

    private Converter converter;
    private SourceConfig sourceConfig;
    private DataSource dataSource;
    private DataTable dataTable;

    public Source(DConvers dconvers, String name, Converter converter, SourceConfig sourceConfig) {
        super(dconvers, name);

        this.converter = converter;
        this.sourceConfig = sourceConfig;

        valid = prepare();
        if (valid) valid = validate();

        log.debug("Source({}) is created", name);
    }

    private boolean prepare() {
        log.debug("Source({}).prepare.", name);
        String dataSourceName = sourceConfig.getDataSource();
        //log.debug("Source.prepare: dataSourceName={}", dataSourceName);
        dataSource = dconvers.getDataSource(dataSourceName.toUpperCase());
        return true;
    }

    @Override
    public boolean validate() {
        log.debug("Source({}).validate.", name);

        if (dataSource == null) {
            error("datasource({}) is not found, required by Converter({})", sourceConfig.getDataSource(), converter.getName());
            dconvers.hasWarning = true;
            return false;
        }

        return true;
    }

    public boolean buildDataTable() {
        log.debug("Source({}).buildDataTable.", name);

        String query = sourceConfig.getQuery();
        if (query == null) {
            return false;
        }

        dataTable = dataSource.getDataTable(sourceConfig.getName(), sourceConfig.getId(), query, sourceConfig.getQueryParameterMap());
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

    /**
     * After print all outputs.
     */
    public void printed() {

        // destroy/release-memory
        if (!sourceConfig.hasTarget()) {
            valid = false;
            converter = null;
            sourceConfig = null;
            dataSource = null;
            dataTable = null;
        }

    }
}
