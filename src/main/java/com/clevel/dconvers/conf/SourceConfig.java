package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceConfig extends Config {

    private String dataSource;
    private String query;
    private String id;
    private String output;

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
        id = properties.getString(source.connectKey(name, Property.ID),"id");
        output = properties.getString(Property.OUTPUT_FILE.key(),"");

        String outputExt = ".sql";
        if (output.length() == 0) {
            output = name + outputExt;
        } else if (!output.endsWith(outputExt)) {
            output = output + outputExt;
        }

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("SourceConfig({}).validateProperties.", name);

        if (dataSource == null) {
            log.error(Property.SOURCE.connectKey(name, Property.DATA_SOURCE) + " is required by source({})", name);
            return false;
        }

        if (query == null) {
            log.error(Property.SOURCE.connectKey(name, Property.QUERY) + " is required by source({})", name);
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

    public String getId() {
        return id;
    }

    public String getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("dataSource", dataSource)
                .append("query", query)
                .append("id", id)
                .append("output", output)
                .toString();
    }
}
