package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.ngin.Crypto;
import javafx.util.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class DataSourceConfig extends Config {

    private String dbms;

    private String url;
    private String driver;
    private String schema;
    private String user;
    private String password;
    private int retry;

    private List<Pair<String, String>> propList;

    private String pre;
    private String post;

    private String host;
    private boolean ssl;

    private String valueQuotes;
    private String nameQuotes;

    public DataSourceConfig(DConvers dconvers, String name) {
        super(dconvers, name);
        properties = dconvers.dataConversionConfigFile.properties;

        valid = loadProperties();
        if (valid) valid = validate();

        log.trace("DataSourceConfig({}) is created with valid={}", name, valid);
    }

    /**
     * asLib: DataSourceConfig from Connection.
     **/
    public DataSourceConfig(DConvers dconvers, Connection connection) {
        super(dconvers, "from connection");
        properties = dconvers.dataConversionConfigFile.properties;

        valid = loadProperties(connection);
        if (valid) valid = validate();

        log.trace("DataSourceConfig({}) is created with valid={}", name, valid);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataSourceConfig.class);
    }

    /**
     * asLib:
     */
    protected boolean loadProperties(Connection connection) {
        log.trace("DataSourceConfig.loadProperties(Connection).");
        propList = new ArrayList<>();

        try {
            DatabaseMetaData metaData = connection.getMetaData();

            url = metaData.getURL();
            driver = metaData.getDriverName();
            schema = metaData.getSchemaTerm();
            user = metaData.getUserName();
            password = "";
            retry = 3;

            pre = "";
            post = "";
            nameQuotes = "";

            String dbms = getDbms().toUpperCase();
            if (dbms.equals("ORACLE")) {
                valueQuotes = "\"";
            } else {
                /*MySQL(MariaDB),AS400*/
                valueQuotes = "'";
            }
        } catch (SQLException ex) {
            error("Load properties from Connection is failed!, ", ex);
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadProperties() {
        log.trace("DataSourceConfig.loadProperties.");
        propList = new ArrayList<>();

        if (properties == null) {
            return false;
        }

        Property dataSource = Property.DATA_SOURCE;

        url = getPropertyString(properties, dataSource.connectKey(name, Property.URL));
        driver = getPropertyString(properties, dataSource.connectKey(name, Property.DRIVER));
        schema = getPropertyString(properties, dataSource.connectKey(name, Property.SCHEMA), "");
        user = getPropertyString(properties, dataSource.connectKey(name, Property.USER));
        password = getPropertyString(properties, dataSource.connectKey(name, Property.PASSWORD));
        retry = properties.getInt(dataSource.connectKey(name, Property.RETRY), 1);

        boolean userEncrypted = properties.getBoolean(dataSource.connectKey(name, Property.USER, Property.ENCRYPTED), false);
        if (userEncrypted) {
            user = Crypto.decrypt(user);
        }

        boolean passwordEncrypted = properties.getBoolean(dataSource.connectKey(name, Property.PASSWORD, Property.ENCRYPTED), false);
        if (passwordEncrypted) {
            password = Crypto.decrypt(password);
        }

        Configuration propProperties = properties.subset(dataSource.connectKey(name, Property.PROPERTIES));
        Iterator<String> propKeyList = propProperties.getKeys();
        for (Iterator<String> it = propKeyList; it.hasNext(); ) {
            String key = it.next();
            propList.add(new Pair<>(key, getPropertyString(propProperties, key)));
        }
        //log.debug("propList = {}", propList);

        pre = getPropertyString(properties, dataSource.connectKey(name, Property.PRE), "");
        post = getPropertyString(properties, dataSource.connectKey(name, Property.POST), "");

        if (properties != null) {
            ssl = properties.getBoolean(dataSource.connectKey(name, Property.SSL), false);
        }
        host = getPropertyString(properties, dataSource.connectKey(name, Property.HOST), "");

        nameQuotes = getPropertyString(properties, dataSource.connectKey(Property.NAME.prefixKey(Property.QUOTES.prefixKey(name))), "");
        valueQuotes = getPropertyString(properties, dataSource.connectKey(Property.VALUE.prefixKey(Property.QUOTES.prefixKey(name))), "\"");

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("DataSourceConfig.validateProperties.");

        /*if (!isEmailDataSource()) {
            if (url == null || driver == null || user == null || password == null) {
                log.debug("some value is null, please check datasource.{} section", name);
                return false;
            }
        }*/

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

    public int getRetry() {
        return retry;
    }

    public List<Pair<String, String>> getPropList() {
        return propList;
    }

    public Properties getPropListAsProperties() {
        Properties properties = new Properties();

        if (propList.size() == 0) {
            return properties;
        }

        for (Pair<String, String> propertyPair : propList) {
            properties.put(propertyPair.getKey(), propertyPair.getValue());
        }

        return properties;
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
                .append("retry", retry)
                .append("propList", propList)
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
