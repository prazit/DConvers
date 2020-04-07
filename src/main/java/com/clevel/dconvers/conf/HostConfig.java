package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Crypto;
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

    public HostConfig(Application application, String name) {
        super(application, name);
        properties = application.dataConversionConfigFile.properties;

        valid = loadProperties();
        if (valid) valid = validate();

        log.trace("SFTPConfig({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(HostConfig.class);
    }

    @Override
    protected boolean loadProperties() {
        log.trace("SFTPConfig.loadProperties.");

        Property sftpProperty = Property.SFTP;

        host = properties.getString(sftpProperty.connectKey(name, Property.HOST), "");
        port = properties.getInt(sftpProperty.connectKey(name, Property.PORT), 22);
        user = properties.getString(sftpProperty.connectKey(name, Property.USER));
        password = properties.getString(sftpProperty.connectKey(name, Property.PASSWORD));
        retry = properties.getInt(sftpProperty.connectKey(name, Property.RETRY), 1);
        tmp = properties.getString(sftpProperty.connectKey(name, Property.TMP), "");

        boolean userEncrypted = properties.getBoolean(sftpProperty.connectKey(name, Property.USER, Property.ENCRYPTED), false);
        if (userEncrypted) {
            user = Crypto.decrypt(user);
        }

        boolean passwordEncrypted = properties.getBoolean(sftpProperty.connectKey(name, Property.PASSWORD, Property.ENCRYPTED), false);
        if (passwordEncrypted) {
            password = Crypto.decrypt(password);
        }

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("SFTPConfig.validateProperties.");

        if (user == null || password == null) {
            log.debug("some value is null, please check sftp.{} section", name);
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
        return application.currentConverter.compileDynamicValues(tmp);
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
}
