package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.TargetConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Target extends AppBase {

    private Converter converter;
    private TargetConfig targetConfig;
    private Map<String, Source> sourceMap;

    private DataTable dataTable;

    public Target(Application application, String name, Converter converter, TargetConfig targetConfig) {
        super(application, name);
        this.converter = converter;
        this.targetConfig = targetConfig;

        valid = prepare();
        if (valid) valid = validate();

        log.trace("Target({}) is created", name);
    }

    private boolean prepare() {
        log.trace("Target({}).prepare.", name);
        sourceMap = converter.getSourceMap();
        return true;
    }

    @Override
    public boolean validate() {

        if (sourceMap == null || sourceMap.size() == 0) {
            log.error("Sources are required for target({})", name);
            return false;
        }

        String sourceName = targetConfig.getSource();
        if (!sourceMap.containsKey(sourceName)) {
            log.error("Source({}) is not found, required by target({})", sourceName, name);
            return false;
        }

        return true;

    }

    public boolean buildDataTable() {

        Source source = sourceMap.get(targetConfig.getSource());
        DataTable sourceDataTable = source.getDataTable();

        dataTable = new DataTable(name);
        for (DataRow row : sourceDataTable.getAllRow()) {

            // TODO: create data-table for each target,


            // TODO: create data-table for mappings (mapping table contains id of source and id of target)

        }

        return dataTable != null;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Target.class);
    }

    public DataTable getDataTable() {
        return dataTable;
    }
}
