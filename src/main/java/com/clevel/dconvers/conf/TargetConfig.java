package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import javafx.util.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TargetConfig extends Config {

    private ConverterConfigFile converterConfigFile;
    private OutputConfig outputConfig;

    private int index;

    private String source;
    private String table;
    private String id;
    private long rowNumberStartAt;

    private List<Pair<String,String>> columnList;

    TargetConfig(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);

        this.converterConfigFile = converterConfigFile;
        properties = converterConfigFile.getProperties();

        valid = loadProperties();
        if (valid) valid = validate();
        if (valid) {
            outputConfig = new OutputConfig(application, Property.TARGET.connectKey(name), properties);
            valid = outputConfig.isValid();
        }

        log.trace("SourceConfig({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(TargetConfig.class);
    }

    @Override
    protected boolean loadProperties() {
        log.trace("TargetConfig({}).loadProperties.", name);

        Configuration targetProperties = properties.subset(Property.TARGET.connectKey(name));
        source = targetProperties.getString(Property.SOURCE.key());
        table = targetProperties.getString(Property.TABLE.key());
        id = targetProperties.getString(Property.ID.key(), "id");
        rowNumberStartAt = targetProperties.getLong(Property.ROW_NUMBER.key(), 1);
        index = targetProperties.getInt(Property.INDEX.key(), 1);

        Configuration columnProperties = targetProperties.subset(Property.COLUMN.key());
        Iterator<String> columnKeyList = columnProperties.getKeys();
        columnList = new ArrayList<>();
        for (Iterator<String> it = columnKeyList; it.hasNext(); ) {
            String key = it.next();
            columnList.add(new Pair<>(key,columnProperties.getString(key)));
        }
        log.debug("columnList = {}", columnList);

        return true;
    }

    @Override
    public boolean validate() {

        Map<String, SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        if (!sourceConfigMap.containsKey(source)) {
            log.error("Invalid source specified, " + Property.TARGET.connectKey(name, Property.SOURCE) + "=" + source);
            return false;
        }

        return true;
    }

    public int getIndex() {
        return index;
    }

    public String getSource() {
        return source;
    }

    public String getTable() {
        return table;
    }

    public String getId() {
        return id;
    }

    public long getRowNumberStartAt() {
        return rowNumberStartAt;
    }

    public List<Pair<String, String>> getColumnList() {
        return columnList;
    }

    public OutputConfig getOutputConfig() {
        return outputConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("outputConfig", outputConfig)
                .append("index", index)
                .append("source", source)
                .append("table", table)
                .append("id", id)
                .append("rowNumberStartAt", rowNumberStartAt)
                .append("columnList", columnList)
                .toString()
                .replace('=', ':');
    }

}