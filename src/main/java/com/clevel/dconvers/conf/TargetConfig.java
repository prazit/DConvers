package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import javafx.util.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConversionException;
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

    private int index;

    private List<String> postUpdate;

    private String source;
    private String output;
    private String table;
    private String id;
    private long rowNumberStartAt;

    private boolean create;
    private boolean insert;
    private boolean markdown;

    private List<Pair<String,String>> columnList;

    TargetConfig(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);

        this.converterConfigFile = converterConfigFile;
        properties = converterConfigFile.getProperties();

        valid = loadProperties();
        if (valid) valid = validate();

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
        output = targetProperties.getString(Property.OUTPUT_FILE.key(),"");
        table = targetProperties.getString(Property.TABLE.key());
        id = targetProperties.getString(Property.ID.key(), "id");
        create = targetProperties.getBoolean(Property.CREATE.key(), false);
        insert = targetProperties.getBoolean(Property.INSERT.key(), true);
        markdown = targetProperties.getBoolean(Property.MARKDOWN.key(), true);
        rowNumberStartAt = targetProperties.getLong(Property.ROW_NUMBER.key(), 1);
        index = targetProperties.getInt(Property.INDEX.key(), 1);

        String outputExt = ".sql";
        if (output.length() == 0) {
            output = table + outputExt;
        } else if (!output.toLowerCase().endsWith(outputExt)) {
            output = output + outputExt;
        }

        Configuration columnProperties = targetProperties.subset(Property.COLUMN.key());
        Iterator<String> columnKeyList = columnProperties.getKeys();
        columnList = new ArrayList<>();
        for (Iterator<String> it = columnKeyList; it.hasNext(); ) {
            String key = it.next();
            columnList.add(new Pair<>(key,columnProperties.getString(key)));
        }
        log.debug("columnList = {}", columnList);

        List<Object> postUpdateObjectList;
        try {
            postUpdateObjectList = targetProperties.getList(Property.POST_UPDATE.key());
        } catch (ConversionException ex) {
            postUpdateObjectList = new ArrayList<>();
        }
        postUpdate = new ArrayList<>();
        for (Object obj : postUpdateObjectList) {
            postUpdate.add(obj.toString());
        }
        log.debug("postUpdate = {}", postUpdate);

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

    public List<String> getPostUpdate() {
        return postUpdate;
    }

    public int getIndex() {
        return index;
    }

    public String getSource() {
        return source;
    }

    public String getOutput() {
        return output;
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

    public boolean isCreate() {
        return create;
    }

    public boolean isInsert() {
        return insert;
    }

    public boolean isMarkdown() {
        return markdown;
    }

    public List<Pair<String, String>> getColumnList() {
        return columnList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("valid", valid)
                .append("name", name)
                .append("table", table)
                .append("create", create)
                .append("insert", insert)
                .append("markdown", markdown)
                .append("source", source)
                .append("output", output)
                .append("columnList", columnList)
                .toString()
                .replace('=', ':');
    }

}