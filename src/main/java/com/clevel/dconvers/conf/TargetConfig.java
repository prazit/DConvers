package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TargetConfig extends Config {

    private String source;
    private String output;
    private String table;

    private boolean create;
    private boolean insert;

    private List<Object> columnList;

    TargetConfig(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);

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

        Property target = Property.TARGET;

        source = properties.getString(target.connectKey(name, Property.SOURCE));
        output = properties.getString(target.connectKey(name, Property.OUTPUT_FILE));
        table = properties.getString(target.connectKey(name, Property.TABLE));
        create = properties.getBoolean(target.connectKey(name, Property.CREATE));
        insert = properties.getBoolean(target.connectKey(name, Property.INSERT));
        columnList = properties.getList(target.connectKey(name, Property.COLUMN));

        return true;
    }

    @Override
    public boolean validate() {


        return true;
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

    public boolean isCreate() {
        return create;
    }

    public boolean isInsert() {
        return insert;
    }

    public List<Object> getColumnList() {
        return columnList;
    }
}
