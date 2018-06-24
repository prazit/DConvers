package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.SourceConfig;
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
        String dataSourceName = sourceConfig.getDataSource();
        dataSource = application.dataSourceMap.get(dataSourceName);

        return true;
    }

    @Override
    public boolean validate() {

        if (dataSource == null) {
            log.warn("datasource({}) is not found, used in Converter({})", sourceConfig.getDataSource(), converter.getName());
            return false;
        }

        return true;
    }

    public boolean loadDataTable() {
        // TODO: load dataTable for Source
        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Source.class);
    }

    public DataTable getDataTable() {
        return dataTable;
    }
}
