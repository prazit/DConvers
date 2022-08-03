package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import javafx.util.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TargetConfig extends Config {

    private OutputConfig mappingOutputConfig;
    private OutputConfig transferOutputConfig;
    private TransformConfig transformConfig;
    private OutputConfig outputConfig;

    private int index;

    private String source;
    private List<String> sourceList;
    private String id;
    private String rowNumberStartAt;

    private List<Pair<String, String>> columnList;

    TargetConfig(DConvers dconvers, String name, ConverterConfigFile converterConfigFile) {
        super(dconvers, name);
        properties = converterConfigFile.getProperties();

        if (!dconvers.getManualMode()) {
            valid = loadProperties();
            if (valid) valid = validate();

            String targetBaseProperty = Property.TARGET.connectKey(name);
            if (valid) {
                outputConfig = new OutputConfig(dconvers, targetBaseProperty, properties);
                valid = outputConfig.isValid();
            }

            if (valid) {
                transferOutputConfig = new OutputConfig(dconvers, Property.TRANSFER.prefixKey(targetBaseProperty), properties);
                valid = outputConfig.isValid();
            }

            if (valid) {
                mappingOutputConfig = new OutputConfig(dconvers, Property.MAPPING.prefixKey(targetBaseProperty), properties);
                valid = outputConfig.isValid();
            }

            if (valid) {
                transformConfig = new TransformConfig(dconvers, targetBaseProperty, properties);
                valid = transformConfig.isValid();
            }
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
        source = getPropertyString(targetProperties, Property.SOURCE.key());
        id = getPropertyString(targetProperties, Property.ID.key(), "id");
        rowNumberStartAt = getPropertyString(targetProperties, Property.ROW_NUMBER.key(), "1");
        index = targetProperties.getInt(Property.INDEX.key(), 1);

        sourceList = new ArrayList<>();
        String[] sources = source.split("[,]");
        for (String src : sources) {
            src = src.trim();
            if ("".equals(src)) {
                continue;
            }
            sourceList.add(src);
        }

        Configuration columnProperties = targetProperties.subset(Property.COLUMN.key());
        Iterator<String> columnKeyList = columnProperties.getKeys();
        columnList = new ArrayList<>();
        for (Iterator<String> it = columnKeyList; it.hasNext(); ) {
            String key = it.next();
            columnList.add(new Pair<>(key, getPropertyString(columnProperties, key)));
        }
        log.debug("columnList = {}", columnList);

        return true;
    }

    @Override
    public boolean validate() {
        return true;
    }

    public int getIndex() {
        return index;
    }

    public String getSource() {
        return source;
    }

    public List<String> getSourceList() {
        return sourceList;
    }

    public String getId() {
        return id;
    }

    public long getRowNumberStartAt() {
        String value = dconvers.currentConverter.compileDynamicValues(rowNumberStartAt);
        if (value == null) {
            return 1;
        }
        return Long.parseLong(value);
    }

    public List<Pair<String, String>> getColumnList() {
        return columnList;
    }

    public OutputConfig getOutputConfig() {
        return outputConfig;
    }

    public OutputConfig getTransferOutputConfig() {
        return transferOutputConfig;
    }

    public OutputConfig getMappingOutputConfig() {
        return mappingOutputConfig;
    }

    public TransformConfig getTransformConfig() {
        return transformConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("outputConfig", outputConfig)
                .append("index", index)
                .append("source", source)
                .append("sourceList", sourceList)
                .append("id", id)
                .append("rowNumberStartAt", rowNumberStartAt)
                .append("columnList", columnList)
                .toString()
                .replace('=', ':');
    }

    public void setMappingOutputConfig(OutputConfig mappingOutputConfig) {
        this.mappingOutputConfig = mappingOutputConfig;
    }

    public void setTransferOutputConfig(OutputConfig transferOutputConfig) {
        this.transferOutputConfig = transferOutputConfig;
    }

    public void setTransformConfig(TransformConfig transformConfig) {
        this.transformConfig = transformConfig;
    }

    public void setOutputConfig(OutputConfig outputConfig) {
        this.outputConfig = outputConfig;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSourceList(List<String> sourceList) {
        this.sourceList = sourceList;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRowNumberStartAt(String rowNumberStartAt) {
        this.rowNumberStartAt = rowNumberStartAt;
    }

    public void setColumnList(List<Pair<String, String>> columnList) {
        this.columnList = columnList;
    }

    @Override
    public void saveProperties() throws ConfigurationException {

        Configuration targetProperties = properties.subset(Property.TARGET.connectKey(name));
        setPropertyString(targetProperties, Property.SOURCE.key(), "", source);
        setPropertyString(targetProperties, Property.ID.key(), "id", id);
        setPropertyString(targetProperties, Property.ROW_NUMBER.key(), "1", rowNumberStartAt);
        setPropertyInt(targetProperties, Property.INDEX.key(), 1, index);

        for (Pair<String, String> column : columnList) {
            setPropertyString(targetProperties, Property.COLUMN.connectKey(column.getKey()), "", column.getValue());
        }

        transformConfig.saveProperties();
        outputConfig.saveProperties();
        mappingOutputConfig.saveProperties();
        transferOutputConfig.saveProperties();

    }
}