package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.SFTP;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Source is a source file (target header file) that contains all the specified target.
 */
public class DataConversionConfigFile extends ConfigFile {

    private Map<String, DataSourceConfig> dataSourceConfigMap;
    private Map<String, SFTPConfig> sftpConfigMap;
    private Map<String, ConverterConfigFile> converterConfigMap;

    private String outputReportPath;
    private String outputSourcePath;
    private String outputTargetPath;
    private String outputMappingPath;

    private String mappingTablePrefix;
    private String reportTableName;

    private int reportFileNumber;
    private int sourceFileNumber;
    private int targetFileNumber;
    private int mappingFileNumber;

    private boolean exitOnError;

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
        outputReportPath = properties.getString(converterProperty.connectKey(Property.REPORT_PATH),"");
        outputSourcePath = properties.getString(converterProperty.connectKey(Property.SOURCE_PATH),"");
        outputTargetPath = properties.getString(converterProperty.connectKey(Property.TARGET_PATH),"");
        outputMappingPath = properties.getString(converterProperty.connectKey(Property.MAPPING_PATH),"");

        reportFileNumber = properties.getInt(converterProperty.connectKey(Property.REPORT_PATH.connectKey(Property.FILE_NUMBER)), 1);
        sourceFileNumber = properties.getInt(converterProperty.connectKey(Property.SOURCE_PATH.connectKey(Property.FILE_NUMBER)), 1);
        targetFileNumber = properties.getInt(converterProperty.connectKey(Property.TARGET_PATH.connectKey(Property.FILE_NUMBER)), 1);
        mappingFileNumber = properties.getInt(converterProperty.connectKey(Property.MAPPING_PATH.connectKey(Property.FILE_NUMBER)), 1);

        mappingTablePrefix = properties.getString(converterProperty.connectKey(Property.MAPPING_PREFIX),"");
        reportTableName = properties.getString(converterProperty.connectKey(Property.REPORT_TABLE),"");

        exitOnError = properties.getBoolean(Property.EXIT_ON_ERROR.key(), true);

        List<Object> dataSourceNameList;
        try {
            dataSourceNameList = properties.getList(Property.DATA_SOURCE.key());
        } catch (ConversionException ex) {
            dataSourceNameList = new ArrayList<>();
            dataSourceNameList.add(properties.getString(Property.DATA_SOURCE.key()));
        }
        dataSourceConfigMap = new HashMap<>();
        String name;
        for (Object object : dataSourceNameList) {
            name = object.toString();
            dataSourceConfigMap.put(name, new DataSourceConfig(application, name));
        }


        List<Object> sftpNameList;
        try {
            sftpNameList = properties.getList(Property.SFTP.key());
        } catch (ConversionException ex) {
            sftpNameList = new ArrayList<>();
            sftpNameList.add(properties.getString(Property.SFTP.key()));
        }
        sftpConfigMap = new HashMap<>();
        for (Object object : sftpNameList) {
            name = object.toString();
            sftpConfigMap.put(name, new SFTPConfig(application, name));
        }


        List<Object> converterNameList;
        try {
            converterNameList = properties.getList(converterProperty.key());
        } catch (ConversionException ex) {
            converterNameList = new ArrayList<>();
            converterNameList.add(properties.getString(converterProperty.key()));
        }
        converterConfigMap = new HashMap<>();
        for (Object object : converterNameList) {
            name = object.toString();
            if (name.lastIndexOf(".") < 0) {
                name = name + Defaults.CONFIG_FILE_EXT.getStringValue();
            }
            converterConfigMap.put(name, new ConverterConfigFile(application, name));
        }


        childValid = true;
        return true;
    }

    @Override
    public boolean validate() {
        log.trace("DataConversionConfigFile.validateProperties.");

        for (DataSourceConfig dataSourceConfig : dataSourceConfigMap.values()) {
            if (!dataSourceConfig.isValid()) {
                log.error("Invalid datasource specified ({})", dataSourceConfig.getName());
                childValid = false;
                return false;
            }
        }

        for (ConverterConfigFile converterConfigFile : converterConfigMap.values()) {
            if (!converterConfigFile.isValid()) {
                log.error("Invalid Converter File ({}) Please see 'sample-converter.conf' for detailed", converterConfigFile.getName());
                childValid = false;
                return false;
            }
        }

        return true;
    }

    // access read only properties

    public String getOutputReportPath() {
        return outputReportPath;
    }

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

    public int getReportFileNumber() {
        return reportFileNumber;
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

    public Map<String, ConverterConfigFile> getConverterConfigMap() {
        return converterConfigMap;
    }

    public Map<String, DataSourceConfig> getDataSourceConfigMap() {
        return dataSourceConfigMap;
    }

    public Map<String, SFTPConfig> getSftpConfigMap() {
        return sftpConfigMap;
    }

    public boolean isChildValid() {
        return childValid;
    }

    public String getMappingTablePrefix() {
        return mappingTablePrefix;
    }

    public String getReportTableName() {
        return reportTableName;
    }

}
