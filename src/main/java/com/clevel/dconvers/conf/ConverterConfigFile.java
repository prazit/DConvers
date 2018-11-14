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
import java.util.Map;

public class ConverterConfigFile extends ConfigFile {

    private Map<String, SourceConfig> sourceConfigMap;
    private Map<String, TargetConfig> targetConfigMap;
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
        log.trace("ConverterConfigFile.loadProperties.");

        index = properties.getInt(Property.CONVERTER_FILE.connectKey(Property.INDEX), 1);

        List<Object> sourceNameList;
        try {
            sourceNameList = properties.getList(Property.SOURCE.key());
        } catch (ConversionException ex) {
            sourceNameList = new ArrayList<>();
            sourceNameList.add(properties.getString(Property.SOURCE.key()));
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
            sourceConfigMap.put(name, sourceConfig);
        }

        List<Object> targetNameList;
        try {
            targetNameList = properties.getList(Property.TARGET.key());
        } catch (ConversionException ex) {
            targetNameList = new ArrayList<>();
            targetNameList.add(properties.getString(Property.TARGET.key()));
        }

        targetConfigMap = new HashMap<>();
        TargetConfig targetConfig;
        for (Object object : targetNameList) {
            name = object.toString();
            targetConfig = new TargetConfig(application, name, this);
            if (!targetConfig.isValid()) {
                return false;
            }
            targetConfigMap.put(name, targetConfig);
        }

        return true;
    }

    @Override
    public boolean validate() {
        log.debug("ConverterConfigFile.validateProperties. has {} sources and {} targets",sourceConfigMap.size(),targetConfigMap.size());

        if (targetConfigMap.size() == 0) {
            log.debug("No target is specified in converter({})", name);
        }

        return true;
    }

    public int getIndex() {
        return index;
    }

    public Map<String, SourceConfig> getSourceConfigMap() {
        return sourceConfigMap;
    }

    public Map<String, TargetConfig> getTargetConfigMap() {
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
