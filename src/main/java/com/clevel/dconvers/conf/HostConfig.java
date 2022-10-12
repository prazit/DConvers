package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.LibraryMode;
import com.clevel.dconvers.ngin.Crypto;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostConfig extends Config {

    private String host;
    private int port;
    private String user;
    private String password;
    private int retry;
    private String tmp;
    private Property rootProperty;

    private boolean userEncrypted;
    private boolean passwordEncrypted;

    /**
     * @param rootProperty Property.SFTP or Property.SMTP
     */
    public HostConfig(DConvers dconvers, String name, Property rootProperty) {
        super(dconvers, name);
        properties = dconvers.dataConversionConfigFile.properties;
        this.rootProperty = rootProperty;

        loadDefaults();
        if (LibraryMode.MANUAL != dconvers.switches.getLibraryMode()) {
            valid = loadProperties();
            if (valid) valid = validate();
        }

        log.debug("SFTPConfig({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(HostConfig.class);
    }

    @Override
    public void loadDefaults() {
        /*TODO: loadDefaults*/
    }

    @Override
    protected boolean loadProperties() {
        log.debug("SFTPConfig.loadProperties.");

        host = getPropertyString(properties, rootProperty.connectKey(name, Property.HOST), "");
        port = properties.getInt(rootProperty.connectKey(name, Property.PORT), 22);
        user = getPropertyString(properties, rootProperty.connectKey(name, Property.USER));
        password = getPropertyString(properties, rootProperty.connectKey(name, Property.PASSWORD));
        retry = properties.getInt(rootProperty.connectKey(name, Property.RETRY), 1);
        tmp = getPropertyString(properties, rootProperty.connectKey(name, Property.TMP), "");

        userEncrypted = properties.getBoolean(rootProperty.connectKey(name, Property.USER, Property.ENCRYPTED), false);
        if (userEncrypted) {
            user = Crypto.decrypt(user);
        }

        passwordEncrypted = properties.getBoolean(rootProperty.connectKey(name, Property.PASSWORD, Property.ENCRYPTED), false);
        if (passwordEncrypted) {
            password = Crypto.decrypt(password);
        }

        return true;
    }

    @Override
    public boolean validate() {
        log.debug("HostConfig.validateProperties.");

        if (user == null || password == null) {
            log.debug("some value is null, please check {}.{} section", rootProperty.name(), name);
            return false;
        }

        return true;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getRetry() {
        return retry;
    }

    public String getTmp() {
        return dconvers.currentConverter.compileDynamicValues(tmp);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("name", name)
                .append("valid", valid)
                .append("host", host)
                .append("port", port)
                .append("user", user)
                .append("retry", retry)
                .append("tmp", tmp)
                .toString()
                .replace('=', ':');
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    public void setUserEncrypted(boolean userEncrypted) {
        this.userEncrypted = userEncrypted;
    }

    public void setPasswordEncrypted(boolean passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    @Override
    public void saveProperties() throws ConfigurationException {
        String hostKey = rootProperty.connectKey(name, Property.HOST);

        setBlancLinesBefore(hostKey, 1);
        setComment(hostKey, name.toUpperCase());

        setPropertyString(properties, hostKey, "", host);
        setPropertyInt(properties, rootProperty.connectKey(name, Property.PORT), 22, port);
        setPropertyString(properties, rootProperty.connectKey(name, Property.USER), "", (userEncrypted ? Crypto.encrypt(user) : user));
        setPropertyString(properties, rootProperty.connectKey(name, Property.PASSWORD), "", (passwordEncrypted ? Crypto.encrypt(password) : password));
        setPropertyInt(properties, rootProperty.connectKey(name, Property.RETRY), 1, retry);
        setPropertyString(properties, rootProperty.connectKey(name, Property.TMP), "/tmp/", tmp);

        setPropertyBoolean(properties, rootProperty.connectKey(name, Property.USER, Property.ENCRYPTED), false, userEncrypted);
        setPropertyBoolean(properties, rootProperty.connectKey(name, Property.PASSWORD, Property.ENCRYPTED), false, passwordEncrypted);

    }
}
