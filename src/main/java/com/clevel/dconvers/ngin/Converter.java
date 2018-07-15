package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.ConverterConfigFile;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.conf.TargetConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Converter extends AppBase {

    private ConverterConfigFile converterConfigFile;
    private Map<String, Source> sourceMap;
    private Map<String, Target> targetMap;

    public Converter(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);

        this.converterConfigFile = converterConfigFile;
        valid = prepare();
        if (valid) {
            valid = validate();
        }

        log.trace("Converter({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Converter.class);
    }

    private boolean prepare() {
        log.trace("Converter({}).prepare", name);

        Map<String,SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        Map<String,TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();

        boolean valid = true;
        String name;
        Source source;
        sourceMap = new HashMap<>();
        for (SourceConfig sourceConfig:sourceConfigMap.values()) {
            name = sourceConfig.getName();
            source = new Source(application, name, this, sourceConfig);
            valid = source.isValid();
            if (!valid) {
                return false;
            }
            sourceMap.put(name, source);
        }

        Target target;
        targetMap = new HashMap<>();
        for (TargetConfig targetConfig : targetConfigMap.values()) {
            name = targetConfig.getName();
            target = new Target(application, name, this, targetConfig);
            valid = target.isValid();
            if (!valid) {
                return false;
            }
            targetMap.put(name, target);
        }

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Converter({}).validate", name);

        if (sourceMap.size() == 0) {
            log.warn("Source not found, need one source at least.");
            return false;
        }

        if (sourceMap.size() == 0) {
            log.warn("Source not found, need one source at least.");
            return false;
        }

        return true;
    }

    public boolean convert() {
        log.trace("Converter({}).convert", name);


        for (Source source : sourceMap.values()) {
            valid = source.buildDataTable();
            if (!valid) {
                return false;
            }
        }

        for (Target target : targetMap.values()) {
            valid = target.buildDataTable();
            if (!valid) {
                return false;
            }
        }

        return true;
    }

    public boolean render() {
        log.trace("Converter({}).render", name);

        // TODO: create output file from target data tables (call dataTable.render(outputStream))

        return true;

    }

    public ConverterConfigFile getConverterConfigFile() {
        return converterConfigFile;
    }

    public Map<String, Source> getSourceMap() {
        return sourceMap;
    }

    public Map<String, Target> getTargetMap() {
        return targetMap;
    }
}