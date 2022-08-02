package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;

public class SourceConfig extends Config {

    private int index;
    private int querySplit;

    private String dataSource;
    private String query;
    private HashMap<String, String> queryParameterMap;
    private String id;
    private boolean target;

    private OutputConfig outputConfig;

    public SourceConfig(DConvers dconvers, String name, ConverterConfigFile converterConfigFile) {
        super(dconvers, name);

        properties = converterConfigFile.getProperties();

        valid = loadProperties();
        if (valid) valid = validate();
        if (valid) {
            outputConfig = new OutputConfig(dconvers, Property.SOURCE.connectKey(name), properties);
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
        id = getPropertyString(properties, source.connectKey(name, Property.ID), "id");
        index = properties.getInt(source.connectKey(name, Property.INDEX), 0);
        querySplit = properties.getInt(source.connectKey(name, Property.QUERY_SPLIT), 0);
        target = properties.getBoolean(source.connectKey(name, Property.TARGET), true);

        Configuration paramProperties = properties.subset(source.connectKey(name, Property.QUERY));
        Iterator<String> paramKeyList = paramProperties.getKeys();
        queryParameterMap = new HashMap<>();
        String paramName;
        String paramValue;
        for (Iterator<String> it = paramKeyList; it.hasNext(); ) {
            paramName = it.next();
            paramValue = getPropertyString(paramProperties, paramName);
            queryParameterMap.put(paramName.toUpperCase(), paramValue);
        }

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
        return dconvers.currentConverter.compileDynamicValues(dataSource);
    }

    public String getQuery() {
        return dconvers.currentConverter.compileDynamicValues(query);
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public int getQuerySplit() {
        return querySplit;
    }

    public boolean hasTarget() {
        return target;
    }

    public OutputConfig getOutputConfig() {
        return outputConfig;
    }

    public HashMap<String, String> getQueryParameterMap() {
        return queryParameterMap;
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

    public void setIndex(int index) {
        this.index = index;
    }

    public void setQuerySplit(int querySplit) {
        this.querySplit = querySplit;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setQueryParameterMap(HashMap<String, String> queryParameterMap) {
        this.queryParameterMap = queryParameterMap;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    public void setOutputConfig(OutputConfig outputConfig) {
        this.outputConfig = outputConfig;
    }

    @Override
    protected void saveProperties() throws ConfigurationException {
        Property source = Property.SOURCE;

        setPropertyString(properties, source.connectKey(name, Property.DATA_SOURCE), "", dataSource);
        setPropertyString(properties, source.connectKey(name, Property.QUERY), "", query);
        setPropertyString(properties, source.connectKey(name, Property.ID), "id", id);
        setPropertyInt(properties, source.connectKey(name, Property.INDEX), 0, index);
        setPropertyInt(properties, source.connectKey(name, Property.QUERY_SPLIT), 0, querySplit);
        setPropertyBoolean(properties, source.connectKey(name, Property.TARGET), true, target);

        Configuration paramProperties = properties.subset(source.connectKey(name, Property.QUERY));
        Iterator<String> paramKeyList = paramProperties.getKeys();
        String propKey = source.connectKey(name, Property.QUERY, Property.PROPERTIES);
        for (String key : queryParameterMap.keySet()) {
            String value = queryParameterMap.get(key);
            setPropertyString(properties, Property.connectKeyString(propKey, key), "", value);
        }

    }
}
