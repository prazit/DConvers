package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceConfig extends Config {

    private String url;
    private String driver;
    private String schema;
    private String user;
    private String password;

    private String pre;
    private String post;

    private boolean generateConverterFile;

    private String host;
    private boolean ssl;

    public DataSourceConfig(Application application, String name) {
        super(application, name);
        properties = application.dataConversionConfigFile.properties;

        valid = loadProperties();
        if (valid) valid = validate();

        log.trace("DataSourceConfig({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataSourceConfig.class);
    }

    @Override
    protected boolean loadProperties() {
        log.trace("DataSourceConfig.loadProperties.");

        Property dataSource = Property.DATA_SOURCE;

        url = properties.getString(dataSource.connectKey(name, Property.URL));
        driver = properties.getString(dataSource.connectKey(name, Property.DRIVER));
        schema = properties.getString(dataSource.connectKey(name, Property.SCHEMA));
        user = properties.getString(dataSource.connectKey(name, Property.USER));
        password = properties.getString(dataSource.connectKey(name, Property.PASSWORD));

        pre = properties.getString(dataSource.connectKey(name, Property.PRE), "");
        post = properties.getString(dataSource.connectKey(name, Property.POST), "");

        generateConverterFile = properties.getBoolean(dataSource.connectKey(name, Property.GENERATE_TARGET), false);

        host = properties.getString(dataSource.connectKey(name, Property.HOST), "");
        ssl = properties.getBoolean(dataSource.connectKey(name, Property.SSL), false);

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("DataSourceConfig.validateProperties.");

        if (!isEmailDataSource()) {
            if (url == null || driver == null || schema == null || user == null || password == null) {
                log.debug("some value is null, please check datasource.{} section", name);
                return false;
            }
        }

        return true;
    }

    @Override
    public Configuration getProperties() {
        return super.getProperties();
    }

    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    public String getSchema() {
        return schema;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isGenerateConverterFile() {
        return generateConverterFile;
    }

    public String getPre() {
        return pre;
    }

    public String getPost() {
        return post;
    }

    public String getHost() {
        return host;
    }

    public boolean isSsl() {
        return ssl;
    }

    public boolean isEmailDataSource() {
        return !host.isEmpty();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("isEmailDataSource", isEmailDataSource())
                .append("host", host)
                .append("ssl", ssl)
                .append("url", url)
                .append("driver", driver)
                .append("schema", schema)
                .append("user", user)
                .append("password", password)
                .toString();
    }
}
