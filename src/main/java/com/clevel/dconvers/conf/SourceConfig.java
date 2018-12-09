package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceConfig extends Config {

    private int index;

    private String dataSource;
    private String query;
    private String id;

    private OutputConfig outputConfig;

    public SourceConfig(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);

        properties = converterConfigFile.getProperties();

        valid = loadProperties();
        if (valid) valid = validate();
        if (valid) {
            outputConfig = new OutputConfig(application, Property.SOURCE.connectKey(name), properties);
            valid = outputConfig.isValid();
        }

        log.trace("SourceConfig({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SourceConfig.class);
    }

    @Override
    protected boolean loadProperties() {
        log.trace("SourceConfig({}).loadProperties.", name);
        
        Property source = Property.SOURCE;

        dataSource = getPropertyString(properties, source.connectKey(name, Property.DATA_SOURCE));
        query = getPropertyString(properties, source.connectKey(name, Property.QUERY));
        id = getPropertyString(properties, source.connectKey(name, Property.ID),"id");
        index = properties.getInt(source.connectKey(name, Property.INDEX), 0);
        
        return true;
    }

    @Override
    public boolean validate() {
        log.trace("SourceConfig({}).validateProperties.", name);

        if (dataSource == null) {
            error(Property.SOURCE.connectKey(name, Property.DATA_SOURCE) + " is required by source({})", name);
            return false;
        }

        if (query == null) {
            error(Property.SOURCE.connectKey(name, Property.QUERY) + " is required by source({})", name);
            return false;
        }

        return true;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getQuery() {
        return application.currentConverter.compileDynamicValues(query);
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public OutputConfig getOutputConfig() {
        return outputConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("dataSource", dataSource)
                .append("query", query)
                .append("id", id)
                .append("index", index)
                .append("name", name)
                .append("valid", valid)
                .toString()
                .replace('=', ':');
    }
}
