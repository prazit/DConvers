package com.clevel.dconvers.conf;

import com.clevel.dconvers.ConfigFileTypes;
import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.ngin.AppBase;
import javafx.util.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Source is a source file (target header file) that contains all targets.
 */
public class DataConversionConfigFile extends ConfigFile {

    private HashMap<String, DataSourceConfig> dataSourceConfigMap;
    private HashMap<String, HostConfig> sftpConfigMap;
    private HashMap<String, HostConfig> smtpConfigMap;
    private HashMap<String, ConverterConfigFile> converterConfigMap;

    /* all plugins list is List<Pair<PluginName,FullQualifiedClassName>>*/
    private List<Pair<String, String>> pluginsCalcList;
    private List<Pair<String, String>> pluginsOutputList;
    private List<Pair<String, String>> pluginsDataSourceList;
    private List<Pair<String, String>> variableList;

    private String outputSourcePath;
    private String outputTargetPath;
    private String outputMappingPath;

    private int sourceFileNumber;
    private int targetFileNumber;
    private int mappingFileNumber;

    private boolean exitOnError;
    private int errorCode;
    private int warningCode;
    private int successCode;

    private boolean childValid;

    public DataConversionConfigFile(DConvers dconvers, String name) {
        super(dconvers, name);

        log.debug("DataConversionConfigFile({}) = {}", name, this);
        log.trace("DataConversionConfigFile({}) is created.", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataConversionConfigFile.class);
    }

    @Override
    public void loadDefaults() {
        outputMappingPath = "";
        outputSourcePath = "";
        outputTargetPath = "";

        mappingFileNumber = 1;
        sourceFileNumber = 1;
        targetFileNumber = 1;

        exitOnError = true;
        successCode = Defaults.EXIT_CODE_SUCCESS.getIntValue();
        errorCode = Defaults.EXIT_CODE_ERROR.getIntValue();
        warningCode = Defaults.EXIT_CODE_WARNING.getIntValue();

        dataSourceConfigMap = new HashMap<>();
        sftpConfigMap = new HashMap<>();
        smtpConfigMap = new HashMap<>();

        converterConfigMap = new HashMap<>();

        pluginsCalcList = new ArrayList<>();
        pluginsOutputList = new ArrayList<>();
        pluginsDataSourceList = new ArrayList<>();
        variableList = new ArrayList<>();
    }

    @Override
    protected boolean loadProperties() {
        log.trace("DataConversionConfigFile.loadProperties.");
        dconvers.dataConversionConfigFile = this;


        Property converterProperty = Property.CONVERTER_FILE;
        outputSourcePath = getPropertyString(properties, converterProperty.connectKey(Property.SOURCE_PATH), outputSourcePath);
        outputTargetPath = getPropertyString(properties, converterProperty.connectKey(Property.TARGET_PATH), outputTargetPath);
        outputMappingPath = getPropertyString(properties, converterProperty.connectKey(Property.MAPPING_PATH), outputMappingPath);

        sourceFileNumber = properties.getInt(converterProperty.connectKey(Property.SOURCE_PATH.connectKey(Property.FILE_NUMBER)), sourceFileNumber);
        targetFileNumber = properties.getInt(converterProperty.connectKey(Property.TARGET_PATH.connectKey(Property.FILE_NUMBER)), targetFileNumber);
        mappingFileNumber = properties.getInt(converterProperty.connectKey(Property.MAPPING_PATH.connectKey(Property.FILE_NUMBER)), mappingFileNumber);

        exitOnError = properties.getBoolean(Property.EXIT_ON_ERROR.key(), exitOnError);
        successCode = properties.getInt(converterProperty.connectKey(Property.EXIT_CODE_SUCCESS.key()), successCode);
        errorCode = properties.getInt(converterProperty.connectKey(Property.EXIT_CODE_ERROR.key()), errorCode);
        warningCode = properties.getInt(converterProperty.connectKey(Property.EXIT_CODE_WARNING.key()), warningCode);


        List<Object> dataSourceNameList;
        try {
            dataSourceNameList = properties.getList(Property.DATA_SOURCE.key());
        } catch (ConversionException ex) {
            dataSourceNameList = new ArrayList<>();
            dataSourceNameList.add(getPropertyString(properties, Property.DATA_SOURCE.key()));
        }
        String name;
        for (Object object : dataSourceNameList) {
            name = object.toString();
            dataSourceConfigMap.put(name.toUpperCase(), new DataSourceConfig(dconvers, name));
        }


        List<Object> sftpNameList;
        try {
            sftpNameList = properties.getList(Property.SFTP.key());
        } catch (ConversionException ex) {
            sftpNameList = new ArrayList<>();
            sftpNameList.add(getPropertyString(properties, Property.SFTP.key()));
        }
        for (Object object : sftpNameList) {
            name = object.toString();
            sftpConfigMap.put(name.toUpperCase(), new HostConfig(dconvers, name, Property.SFTP));
        }


        List<Object> smtpNameList;
        try {
            smtpNameList = properties.getList(Property.SMTP.key());
        } catch (ConversionException ex) {
            smtpNameList = new ArrayList<>();
            smtpNameList.add(getPropertyString(properties, Property.SMTP.key()));
        }
        for (Object object : smtpNameList) {
            name = object.toString();
            smtpConfigMap.put(name.toUpperCase(), new HostConfig(dconvers, name, Property.SMTP));
        }


        List<Object> converterNameObjectList;
        try {
            converterNameObjectList = properties.getList(converterProperty.key());
        } catch (ConversionException ex) {
            converterNameObjectList = new ArrayList<>();
            converterNameObjectList.add(getPropertyString(properties, converterProperty.key()));
        }
        for (Object object : converterNameObjectList) {
            name = object.toString();
            if (name.lastIndexOf(".") < 0) {
                name = name + Defaults.CONFIG_FILE_EXT.getStringValue();
            }
            converterConfigMap.put(name.toUpperCase(), new ConverterConfigFile(dconvers, name));
        }

        loadStringPairListTo(pluginsCalcList, properties.subset(Property.PLUGINS.connectKey(Property.CALCULATOR)));
        loadStringPairListTo(pluginsOutputList, properties.subset(Property.PLUGINS.connectKey(Property.OUTPUT_FILE)));
        loadStringPairListTo(pluginsDataSourceList, properties.subset(Property.PLUGINS.connectKey(Property.DATA_SOURCE)));
        loadStringPairListTo(variableList, properties.subset(Property.VARIABLE.key()));

        childValid = true;
        return true;
    }

    private void loadStringPairListTo(List<Pair<String, String>> pluginsList, Configuration pluginsProperties) {
        Iterator<String> columnKeyList = pluginsProperties.getKeys();
        String key;
        for (Iterator<String> iterator = columnKeyList; iterator.hasNext(); ) {
            key = iterator.next();
            pluginsList.add(new Pair<>(key, getPropertyString(pluginsProperties, key)));
        }
    }

    @Override
    public boolean validate() {
        log.trace("DataConversionConfigFile.validateProperties.");

        for (DataSourceConfig dataSourceConfig : dataSourceConfigMap.values()) {
            if (!dataSourceConfig.isValid()) {
                //try validate once for manualMode.
                if (!dataSourceConfig.validate()) {
                    error("Invalid datasource specified ({})", dataSourceConfig.getName());
                    childValid = false;
                    return false;
                }
            }
        }

        for (ConverterConfigFile converterConfigFile : converterConfigMap.values()) {
            if (!converterConfigFile.isValid()) {
                //try validate once for manualMode.
                if (!converterConfigFile.validate()) {
                    error("Invalid Converter File ({}) Please see 'sample-converter.conf' for detailed", converterConfigFile.getName());
                    childValid = false;
                    return false;
                }
            }
        }

        return true;
    }

    // access read only properties

    public String getOutputSourcePath() {
        return outputSourcePath;
    }

    public String getOutputTargetPath() {
        return outputTargetPath;
    }

    public String getOutputMappingPath() {
        return outputMappingPath;
    }

    public boolean isExitOnError() {
        return exitOnError;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getWarningCode() {
        return warningCode;
    }

    public int getSuccessCode() {
        return successCode;
    }

    public int getSourceFileNumber() {
        return sourceFileNumber;
    }

    public int getTargetFileNumber() {
        return targetFileNumber;
    }

    public int getMappingFileNumber() {
        return mappingFileNumber;
    }

    public HashMap<String, ConverterConfigFile> getConverterConfigMap() {
        return converterConfigMap;
    }

    public HashMap<String, DataSourceConfig> getDataSourceConfigMap() {
        return dataSourceConfigMap;
    }

    public HashMap<String, HostConfig> getSftpConfigMap() {
        return sftpConfigMap;
    }

    public HashMap<String, HostConfig> getSmtpConfigMap() {
        return smtpConfigMap;
    }

    public List<Pair<String, String>> getPluginsCalcList() {
        return pluginsCalcList;
    }

    public List<Pair<String, String>> getPluginsOutputList() {
        return pluginsOutputList;
    }

    public List<Pair<String, String>> getPluginsDataSourceList() {
        return pluginsDataSourceList;
    }

    public List<Pair<String, String>> getVariableList() {
        return variableList;
    }

    public boolean isChildValid() {
        return childValid;
    }

    public void setOutputSourcePath(String outputSourcePath) {
        this.outputSourcePath = outputSourcePath;
    }

    public void setOutputTargetPath(String outputTargetPath) {
        this.outputTargetPath = outputTargetPath;
    }

    public void setOutputMappingPath(String outputMappingPath) {
        this.outputMappingPath = outputMappingPath;
    }

    public void setSourceFileNumber(int sourceFileNumber) {
        this.sourceFileNumber = sourceFileNumber;
    }

    public void setTargetFileNumber(int targetFileNumber) {
        this.targetFileNumber = targetFileNumber;
    }

    public void setMappingFileNumber(int mappingFileNumber) {
        this.mappingFileNumber = mappingFileNumber;
    }

    public void setExitOnError(boolean exitOnError) {
        this.exitOnError = exitOnError;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setWarningCode(int warningCode) {
        this.warningCode = warningCode;
    }

    public void setSuccessCode(int successCode) {
        this.successCode = successCode;
    }

    public void setDataSourceConfigMap(HashMap<String, DataSourceConfig> dataSourceConfigMap) {
        assert dataSourceConfigMap != null;
        this.dataSourceConfigMap = dataSourceConfigMap;
    }

    public void setSftpConfigMap(HashMap<String, HostConfig> sftpConfigMap) {
        assert sftpConfigMap != null;
        this.sftpConfigMap = sftpConfigMap;
    }

    public void setSmtpConfigMap(HashMap<String, HostConfig> smtpConfigMap) {
        assert smtpConfigMap != null;
        this.smtpConfigMap = smtpConfigMap;
    }

    public void setConverterConfigMap(HashMap<String, ConverterConfigFile> converterConfigMap) {
        assert converterConfigMap != null;
        this.converterConfigMap = converterConfigMap;
    }

    public void setPluginsCalcList(List<Pair<String, String>> pluginsCalcList) {
        assert pluginsCalcList != null;
        this.pluginsCalcList = pluginsCalcList;
    }

    public void setPluginsOutputList(List<Pair<String, String>> pluginsOutputList) {
        assert pluginsOutputList != null;
        this.pluginsOutputList = pluginsOutputList;
    }

    public void setPluginsDataSourceList(List<Pair<String, String>> pluginsDataSourceList) {
        assert pluginsDataSourceList != null;
        this.pluginsDataSourceList = pluginsDataSourceList;
    }

    public void setVariableList(List<Pair<String, String>> variableList) {
        assert variableList != null;
        this.variableList = variableList;
    }

    @Override
    public void saveProperties() throws ConfigurationException {
        saveProperties(null);
    }

    @Override
    public void saveProperties(OutputStream outputStream) throws ConfigurationException {

        valid = validate();
        if (!valid) throw new ConfigurationException("properties are invalid! saveProperties is not allowed.");
        properties.clear();

        setHeaderComment("Saved using " + dconvers.systemVariableMap.get(SystemVariable.APPLICATION_FULL_VERSION).getValue());

        /*save all plugins*/
        for (Pair<String, String> plugin : pluginsCalcList) setPropertyString(properties, Property.PLUGINS.connectKey(Property.CALCULATOR.connectKey(plugin.getKey())), "", plugin.getValue());
        for (Pair<String, String> plugin : pluginsOutputList) setPropertyString(properties, Property.PLUGINS.connectKey(Property.OUTPUT_FILE.connectKey(plugin.getKey())), "", plugin.getValue());
        for (Pair<String, String> plugin : pluginsDataSourceList) setPropertyString(properties, Property.PLUGINS.connectKey(Property.DATA_SOURCE.connectKey(plugin.getKey())), "", plugin.getValue());

        /*save all variables*/
        for (Pair<String, String> plugin : variableList) setPropertyString(properties, Property.PLUGINS.connectKey(Property.VARIABLE.connectKey(plugin.getKey())), "", plugin.getValue());

        /*save all properties*/
        Property converterProperty = Property.CONVERTER_FILE;
        setPropertyString(properties, converterProperty.connectKey(Property.SOURCE_PATH), "", outputSourcePath);
        setPropertyString(properties, converterProperty.connectKey(Property.TARGET_PATH), "", outputTargetPath);
        setPropertyString(properties, converterProperty.connectKey(Property.MAPPING_PATH), "", outputMappingPath);

        setPropertyInt(properties, converterProperty.connectKey(Property.SOURCE_PATH.connectKey(Property.FILE_NUMBER)), 1, sourceFileNumber);
        setPropertyInt(properties, converterProperty.connectKey(Property.TARGET_PATH.connectKey(Property.FILE_NUMBER)), 1, targetFileNumber);
        setPropertyInt(properties, converterProperty.connectKey(Property.MAPPING_PATH.connectKey(Property.FILE_NUMBER)), 1, mappingFileNumber);

        setPropertyBoolean(properties, Property.EXIT_ON_ERROR.key(), true, exitOnError);
        setPropertyInt(properties, converterProperty.connectKey(Property.EXIT_CODE_SUCCESS.key()), Defaults.EXIT_CODE_SUCCESS.getIntValue(), successCode);
        setPropertyInt(properties, converterProperty.connectKey(Property.EXIT_CODE_ERROR.key()), Defaults.EXIT_CODE_ERROR.getIntValue(), errorCode);
        setPropertyInt(properties, converterProperty.connectKey(Property.EXIT_CODE_WARNING.key()), Defaults.EXIT_CODE_WARNING.getIntValue(), warningCode);

        /*save converter file name list*/
        String saveName;
        for (ConverterConfigFile converterConfigFile : converterConfigMap.values()) {
            saveName = converterConfigFile.getSaveName();
            addPropertyString(properties, converterProperty.key(), "", saveName == null ? converterConfigFile.getName() : saveName);
        }

        /*save all dataSources*/
        setBlancLinesBefore(Property.DATA_SOURCE.key(), 1);
        setComment(Property.DATA_SOURCE.key(), "DATASOURCES");
        for (DataSourceConfig dataSourceConfig : dataSourceConfigMap.values().stream().sorted(Comparator.comparing(AppBase::getName)).collect(Collectors.toList())) {
            addPropertyString(properties, Property.DATA_SOURCE.key(), "", dataSourceConfig.getName());
            dataSourceConfig.saveProperties();
        }

        /*save all smtp*/
        for (HostConfig smtpConfig : smtpConfigMap.values().stream().sorted(Comparator.comparing(AppBase::getName)).collect(Collectors.toList())) {
            addPropertyString(properties, Property.SMTP.key(), "", smtpConfig.getName());
            smtpConfig.saveProperties();
        }

        /*save all sftp*/
        setBlancLinesBefore(Property.SFTP.key(), 1);
        setComment(Property.SFTP.key(), "FTP CONECTIONS");
        for (HostConfig sftpConfig : sftpConfigMap.values().stream().sorted(Comparator.comparing(AppBase::getName)).collect(Collectors.toList())) {
            addPropertyString(properties, Property.SFTP.key(), "", sftpConfig.getName());
            sftpConfig.saveProperties();
        }

        /*commit all properties*/
        if (outputStream == null) {
            propertiesBuilder.save();

            /*save other files*/
            for (ConverterConfigFile converterConfigFile : converterConfigMap.values()) {
                converterConfigFile.saveProperties();
            }
        } else {
            propertiesBuilder.getFileHandler().save(outputStream);
        }
    }
}
