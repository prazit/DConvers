package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.configuration2.ex.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConverterConfigFile extends ConfigFile {

    private Map<String, SourceConfig> sourceConfigMap;
    private Map<String, TargetConfig> targetConfigMap;

    public ConverterConfigFile(Application application, String name) {
        super(application, name);
        log.trace("ConverterConfigFile({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConverterConfigFile.class);
    }

    @Override
    protected boolean loadProperties() {
        log.trace("ConverterConfigFile.loadProperties.");

        List<Object> sourceNameList;
        try {
            sourceNameList = properties.getList(Property.SOURCE.key());
        } catch (ConversionException ex) {
            sourceNameList = new ArrayList<>();
            sourceNameList.add(properties.getString(Property.SOURCE.key()));
        }

        sourceConfigMap = new HashMap<>();
        String name;
        for (Object object : sourceNameList) {
            name = object.toString();
            sourceConfigMap.put(name, new SourceConfig(application, name, this));
        }

        List<Object> targetNameList;
        try {
            targetNameList = properties.getList(Property.DATA_SOURCE.key());
        } catch (ConversionException ex) {
            targetNameList = new ArrayList<>();
            targetNameList.add(properties.getString(Property.DATA_SOURCE.key()));
        }

        targetConfigMap = new HashMap<>();
        for (Object object : targetNameList) {
            name = object.toString();
            targetConfigMap.put(name, new TargetConfig(application, name, this));
        }

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("ConverterConfigFile.validateProperties.");
        return true;
    }

}
