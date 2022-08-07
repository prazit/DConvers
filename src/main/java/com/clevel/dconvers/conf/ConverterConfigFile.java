package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ConverterConfigFile extends ConfigFile {

    private HashMap<String, SourceConfig> sourceConfigMap;
    private HashMap<String, TargetConfig> targetConfigMap;
    private int index;

    public ConverterConfigFile(DConvers dconvers, String name) {
        super(dconvers, name);
        log.debug("ConverterConfigFile({}) = {}", name, this);
        log.trace("ConverterConfigFile({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConverterConfigFile.class);
    }

    @Override
    public void loadDefaults() {
        index = 0;

        sourceConfigMap = new HashMap<>();
        targetConfigMap = new HashMap<>();
    }

    @Override
    protected boolean loadProperties() {
        // skip load properties here to avoid plugins not found, then load again after all plugins are loaded in the DConvers.start function.
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
        SourceConfig sourceConfig;
        String name;
        for (Object object : sourceNameList) {
            name = object.toString();
            sourceConfig = new SourceConfig(dconvers, name, this);
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
        TargetConfig targetConfig;
        for (Object object : targetNameList) {
            name = object.toString();
            targetConfig = new TargetConfig(dconvers, name, this);
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

    public void setSourceConfigMap(HashMap<String, SourceConfig> sourceConfigMap) {
        this.sourceConfigMap = sourceConfigMap;
    }

    public void setTargetConfigMap(HashMap<String, TargetConfig> targetConfigMap) {
        this.targetConfigMap = targetConfigMap;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    @Override
    public void saveProperties() throws ConfigurationException {
        saveProperties(null);
    }

    @Override
    public void saveProperties(OutputStream outputStream) throws ConfigurationException {
        setHeaderComment("Saved using " + dconvers.systemVariableMap.get(SystemVariable.APPLICATION_FULL_VERSION).getValue());

        setPropertyInt(properties, Property.CONVERTER_FILE.connectKey(Property.INDEX), 0, index);

        setBlancLinesBefore(Property.SOURCE.key(), 1);
        setComment(Property.SOURCE.key(), "DATA TABLES");
        for (SourceConfig sourceConfig : sourceConfigMap.values()) {
            addPropertyString(properties, Property.SOURCE.key(), "", sourceConfig.getName());
            sourceConfig.saveProperties();
        }

        setBlancLinesBefore(Property.TARGET.key(), 1);
        setComment(Property.TARGET.key(), "TRANSFORM TABLES");
        for (TargetConfig targetConfig : targetConfigMap.values()) {
            addPropertyString(properties, Property.TARGET.key(), "", targetConfig.getName());
            targetConfig.saveProperties();
        }

        if (outputStream == null) {
            propertiesBuilder.save();
        }else{
            propertiesBuilder.getFileHandler().save(outputStream);
        }

    }
}
