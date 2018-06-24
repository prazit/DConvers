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

        sourceMap = converter.getSourceMap();

        return true;
    }

    @Override
    public boolean validate() {

        if (sourceMap == null) {
            log.warn("sources are required, for target({})", name);
            return false;
        }

        return true;
    }

    public boolean loadDataTable() {
        // TODO: load dataTable for Target
        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Target.class);
    }

    public DataTable getDataTable() {
        return dataTable;
    }
}
