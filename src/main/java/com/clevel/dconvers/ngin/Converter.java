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
        valid = true;

        log.trace("Converter({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Converter.class);
    }

    public boolean launch() {
        log.trace("Converter.launch.");

        Map<String,SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        Map<String,TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();

        String name;
        Source source;
        sourceMap = new HashMap<>();
        for (SourceConfig sourceConfig:sourceConfigMap.values()) {
            name = sourceConfig.getName();
            source = new Source(application, name, this, sourceConfig);
            sourceMap.put(name, source);
        }

        // TODO: create targets and mappings

        return createOutput();
    }

    private Source loadSource() {
        return null;
    }

    private Target loadTarget() {
        return null;
    }

    private boolean createOutput() {
        // TODO: create output file
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