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

/**
 * Source is a source file (target header file) that contains all the specified target.
 */
public class DataConversionConfigFile extends ConfigFile {

    private Map<String, DataSourceConfig> dataSourceConfigMap;
    private Map<String, ConverterConfigFile> converterConfigMap;

    private String converterRootPath;
    private String mappingTablePrefix;
    private String reportTableName;

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
        converterRootPath = properties.getString(converterProperty.connectKey(Property.OUTPUT_PATH),"");
        mappingTablePrefix = properties.getString(converterProperty.connectKey(Property.MAPPING_PREFIX),"");
        reportTableName = properties.getString(converterProperty.connectKey(Property.REPORT_TABLE),"");

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
                name = name + Defaults.CONFIG_FILE_EXT.getDefaultValue();
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

    public String getConverterRootPath() {
        return converterRootPath;
    }

    public Map<String, ConverterConfigFile> getConverterConfigMap() {
        return converterConfigMap;
    }

    public Map<String, DataSourceConfig> getDataSourceConfigMap() {
        return dataSourceConfigMap;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("valid", valid)
                .append("childValid", childValid)
                .append("name", name)
                .append("converterRootPath", converterRootPath)
                .append("reportTableName", reportTableName)
                .append("mappingTablePrefix", mappingTablePrefix)
                .append("dataSourceConfigMap", dataSourceConfigMap)
                .append("converterConfigMap", converterConfigMap)
                .toString()
                .replace('=', ':');
    }
}
