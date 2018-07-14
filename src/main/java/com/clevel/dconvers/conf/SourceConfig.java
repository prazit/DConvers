package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceConfig extends Config {

    private String dataSource;
    private String query;

    public SourceConfig(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);

        properties = converterConfigFile.getProperties();

        valid = loadProperties();
        if (valid) valid = validate();

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

        dataSource = properties.getString(source.connectKey(name, Property.DATA_SOURCE));
        query = properties.getString(source.connectKey(name, Property.QUERY));

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("SourceConfig({}).validateProperties.", name);

        if (dataSource == null) {
            log.warn(Property.SOURCE.connectKey(name, Property.DATA_SOURCE) + " is required");
            return false;
        }

        if (query == null) {
            log.warn(Property.SOURCE.connectKey(name, Property.QUERY) + " is required");
            return false;
        }

        return true;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("dataSource", dataSource)
                .append("query", query)
                .toString();
    }
}
