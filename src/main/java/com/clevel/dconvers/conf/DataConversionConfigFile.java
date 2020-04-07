package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import javafx.util.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

    public DataConversionConfigFile(Application application, String name) {
        super(application, name);

        log.debug("DataConversionConfigFile({}) = {}", name, this);
        log.trace("DataConversionConfigFile({}) is created.", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataConversionConfigFile.class);
    }

    @Override
    protected boolean loadProperties() {
        log.trace("DataConversionConfigFile.loadProperties.");
        application.dataConversionConfigFile = this;


        Property converterProperty = Property.CONVERTER_FILE;
        outputSourcePath = getPropertyString(properties, converterProperty.connectKey(Property.SOURCE_PATH), "");
        outputTargetPath = getPropertyString(properties, converterProperty.connectKey(Property.TARGET_PATH), "");
        outputMappingPath = getPropertyString(properties, converterProperty.connectKey(Property.MAPPING_PATH), "");

        sourceFileNumber = properties.getInt(converterProperty.connectKey(Property.SOURCE_PATH.connectKey(Property.FILE_NUMBER)), 1);
        targetFileNumber = properties.getInt(converterProperty.connectKey(Property.TARGET_PATH.connectKey(Property.FILE_NUMBER)), 1);
        mappingFileNumber = properties.getInt(converterProperty.connectKey(Property.MAPPING_PATH.connectKey(Property.FILE_NUMBER)), 1);

        exitOnError = properties.getBoolean(Property.EXIT_ON_ERROR.key(), true);
        successCode = properties.getInt(converterProperty.connectKey(Property.EXIT_CODE_SUCCESS.key()), Defaults.EXIT_CODE_SUCCESS.getIntValue());
        errorCode = properties.getInt(converterProperty.connectKey(Property.EXIT_CODE_ERROR.key()), Defaults.EXIT_CODE_ERROR.getIntValue());
        warningCode = properties.getInt(converterProperty.connectKey(Property.EXIT_CODE_WARNING.key()), Defaults.EXIT_CODE_WARNING.getIntValue());


        List<Object> dataSourceNameList;
        try {
            dataSourceNameList = properties.getList(Property.DATA_SOURCE.key());
        } catch (ConversionException ex) {
            dataSourceNameList = new ArrayList<>();
            dataSourceNameList.add(getPropertyString(properties, Property.DATA_SOURCE.key()));
        }
        dataSourceConfigMap = new HashMap<>();
        String name;
        for (Object object : dataSourceNameList) {
            name = object.toString();
            dataSourceConfigMap.put(name.toUpperCase(), new DataSourceConfig(application, name));
        }


        List<Object> sftpNameList;
        try {
            sftpNameList = properties.getList(Property.SFTP.key());
        } catch (ConversionException ex) {
            sftpNameList = new ArrayList<>();
            sftpNameList.add(getPropertyString(properties, Property.SFTP.key()));
        }
        sftpConfigMap = new HashMap<>();
        for (Object object : sftpNameList) {
            name = object.toString();
            sftpConfigMap.put(name.toUpperCase(), new HostConfig(application, name));
        }


        List<Object> smtpNameList;
        try {
            smtpNameList = properties.getList(Property.SMTP.key());
        } catch (ConversionException ex) {
            smtpNameList = new ArrayList<>();
            smtpNameList.add(getPropertyString(properties, Property.SMTP.key()));
        }
        smtpConfigMap = new HashMap<>();
        for (Object object : smtpNameList) {
            name = object.toString();
            smtpConfigMap.put(name.toUpperCase(), new HostConfig(application, name));
        }


        List<Object> converterNameList;
        try {
            converterNameList = properties.getList(converterProperty.key());
        } catch (ConversionException ex) {
            converterNameList = new ArrayList<>();
            converterNameList.add(getPropertyString(properties, converterProperty.key()));
        }
        converterConfigMap = new HashMap<>();
        for (Object object : converterNameList) {
            name = object.toString();
            if (name.lastIndexOf(".") < 0) {
                name = name + Defaults.CONFIG_FILE_EXT.getStringValue();
            }
            converterConfigMap.put(name.toUpperCase(), new ConverterConfigFile(application, name));
        }


        pluginsCalcList = loadStringPairList(properties.subset(Property.PLUGINS.connectKey(Property.CALCULATOR)));
        pluginsOutputList = loadStringPairList(properties.subset(Property.PLUGINS.connectKey(Property.OUTPUT_FILE)));
        pluginsDataSourceList = loadStringPairList(properties.subset(Property.PLUGINS.connectKey(Property.DATA_SOURCE)));
        variableList = loadStringPairList(properties.subset(Property.VARIABLE.key()));

        childValid = true;
        return true;
    }

    private List<Pair<String, String>> loadStringPairList(Configuration pluginsProperties) {
        List<Pair<String, String>> pluginsList = new ArrayList<>();
        Iterator<String> columnKeyList = pluginsProperties.getKeys();
        String key;
        for (Iterator<String> iterator = columnKeyList; iterator.hasNext(); ) {
            key = iterator.next();
            pluginsList.add(new Pair<>(key, getPropertyString(pluginsProperties, key)));
        }
        //log.debug("pluginsCalcList = {}", pluginsList);
        return pluginsList;

    }

    @Override
    public boolean validate() {
        log.trace("DataConversionConfigFile.validateProperties.");

        for (DataSourceConfig dataSourceConfig : dataSourceConfigMap.values()) {
            if (!dataSourceConfig.isValid()) {
                error("Invalid datasource specified ({})", dataSourceConfig.getName());
                childValid = false;
                return false;
            }
        }

        for (ConverterConfigFile converterConfigFile : converterConfigMap.values()) {
            if (!converterConfigFile.isValid()) {
                error("Invalid Converter File ({}) Please see 'sample-converter.conf' for detailed", converterConfigFile.getName());
                childValid = false;
                return false;
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

}
