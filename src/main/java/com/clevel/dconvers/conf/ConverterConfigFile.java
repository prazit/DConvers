package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConverterConfigFile extends ConfigFile {

    private HashMap<String, SourceConfig> sourceConfigMap;
    private HashMap<String, TargetConfig> targetConfigMap;
    private int index;

    public ConverterConfigFile(Application application, String name) {
        super(application, name);
        log.debug("ConverterConfigFile({}) = {}", name, this);
        log.trace("ConverterConfigFile({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConverterConfigFile.class);
    }

    @Override
    protected boolean loadProperties() {
        // skip load properties here to avoid plugins not found, then load again after all plugins are loaded in the Application.start function.
        return true;
    }

    public boolean loadConfig() {
        log.trace("ConverterConfigFile.loadConfig.");

        index = properties.getInt(Property.CONVERTER_FILE.connectKey(Property.INDEX), 1);

        List<Object> sourceNameList;
        try {
            sourceNameList = properties.getList(Property.SOURCE.key());
        } catch (ConversionException ex) {
            sourceNameList = new ArrayList<>();
            sourceNameList.add(getPropertyString(properties, Property.SOURCE.key()));
        }

        sourceConfigMap = new HashMap<>();
        SourceConfig sourceConfig;
        String name;
        for (Object object : sourceNameList) {
            name = object.toString();
            sourceConfig = new SourceConfig(application, name, this);
            if (!sourceConfig.isValid()) {
                return false;
            }
            sourceConfigMap.put(name.toUpperCase(), sourceConfig);
        }

        List<Object> targetNameList;
        try {
            targetNameList = properties.getList(Property.TARGET.key());
        } catch (ConversionException ex) {
            targetNameList = new ArrayList<>();
            targetNameList.add(getPropertyString(properties, Property.TARGET.key()));
        }

        targetConfigMap = new HashMap<>();
        TargetConfig targetConfig;
        for (Object object : targetNameList) {
            name = object.toString();
            targetConfig = new TargetConfig(application, name, this);
            if (!targetConfig.isValid()) {
                return false;
            }
            targetConfigMap.put(name.toUpperCase(), targetConfig);
        }

        return true;
    }

    @Override
    public boolean validate() {
        return true;
    }

    public int getIndex() {
        return index;
    }

    public HashMap<String, SourceConfig> getSourceConfigMap() {
        return sourceConfigMap;
    }

    public HashMap<String, TargetConfig> getTargetConfigMap() {
        return targetConfigMap;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("valid", valid)
                .append("index", index)
                .append("name", name)
                .append("sourceConfigMap", sourceConfigMap)
                .append("targetConfigMap", targetConfigMap)
                .toString()
                .replace('=', ':');
    }
}
