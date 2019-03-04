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

public class DataSourceConfig extends Config {

    private String dbms;

    private String url;
    private String driver;
    private String schema;
    private String user;
    private String password;

    private List<Pair<String, String>> propList;

    private String pre;
    private String post;

    private String host;
    private boolean ssl;

    private String valueQuotes;
    private String nameQuotes;

    public DataSourceConfig(Application application, String name) {
        super(application, name);
        properties = application.dataConversionConfigFile.properties;

        valid = loadProperties();
        if (valid) valid = validate();

        log.trace("DataSourceConfig({}) is created with valid={}", name, valid);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataSourceConfig.class);
    }

    @Override
    protected boolean loadProperties() {
        log.trace("DataSourceConfig.loadProperties.");
        if (properties == null) {
            return false;
        }

        Property dataSource = Property.DATA_SOURCE;

        url = getPropertyString(properties, dataSource.connectKey(name, Property.URL));
        driver = getPropertyString(properties, dataSource.connectKey(name, Property.DRIVER));
        schema = getPropertyString(properties, dataSource.connectKey(name, Property.SCHEMA), "");
        user = getPropertyString(properties, dataSource.connectKey(name, Property.USER));
        password = getPropertyString(properties, dataSource.connectKey(name, Property.PASSWORD));

        Configuration propProperties = properties.subset(dataSource.connectKey(name, Property.PROPERTIES));
        Iterator<String> propKeyList = propProperties.getKeys();
        propList = new ArrayList<>();
        for (Iterator<String> it = propKeyList; it.hasNext(); ) {
            String key = it.next();
            propList.add(new Pair<>(key, getPropertyString(propProperties, key)));
        }
        log.debug("propList = {}", propList);

        pre = getPropertyString(properties, dataSource.connectKey(name, Property.PRE), "");
        post = getPropertyString(properties, dataSource.connectKey(name, Property.POST), "");

        if (properties != null) {
            ssl = properties.getBoolean(dataSource.connectKey(name, Property.SSL), false);
        }
        host = getPropertyString(properties, dataSource.connectKey(name, Property.HOST), "");

        valueQuotes = getPropertyString(properties, dataSource.connectKey(Property.VALUE.prefixKey(Property.QUOTES.prefixKey(name))), "\"");
        nameQuotes = getPropertyString(properties, dataSource.connectKey(Property.NAME.prefixKey(Property.QUOTES.prefixKey(name))), "\"");

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("DataSourceConfig.validateProperties.");

        if (!isEmailDataSource()) {
            if (url == null || driver == null || user == null || password == null) {
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

    public String getDbms() {
        if (dbms == null && url != null) {
            String[] urls = url.split("[:]");
            if (urls.length > 1) {
                dbms = urls[1];
            }
        }
        return dbms;
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

    public List<Pair<String, String>> getPropList() {
        return propList;
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

    public String getValueQuotes() {
        return valueQuotes;
    }

    public String getNameQuotes() {
        return nameQuotes;
    }

    public boolean isEmailDataSource() {
        return !host.isEmpty();
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof DataSourceConfig)) {
            return false;
        }

        DataSourceConfig dataSourceConfig = (DataSourceConfig) object;
        if (!dataSourceConfig.getUrl().equalsIgnoreCase(url)) {
            return false;
        }

        if (!dataSourceConfig.getDriver().equalsIgnoreCase(driver)) {
            return false;
        }

        if (!dataSourceConfig.getSchema().equalsIgnoreCase(schema)) {
            return false;
        }

        if (!dataSourceConfig.getUser().equalsIgnoreCase(user)) {
            return false;
        }

        if (!dataSourceConfig.getPassword().equalsIgnoreCase(password)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("dbms", dbms)
                .append("url", url)
                .append("driver", driver)
                .append("schema", schema)
                .append("user", user)
                .append("password", password)
                .append("pre", pre)
                .append("post", post)
                .append("host", host)
                .append("ssl", ssl)
                .append("valueQuotes", valueQuotes)
                .append("nameQuotes", nameQuotes)
                .append("name", name)
                .append("valid", valid)
                .toString()
                .replace('=', ':');
    }
}
