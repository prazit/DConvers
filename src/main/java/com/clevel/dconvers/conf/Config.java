package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.ngin.AppBase;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public abstract class Config extends AppBase {

    //-- Shared for Read

    protected Configuration properties;

    public Configuration getProperties() {
        return properties;
    }

    protected String getPropertyString(Configuration properties, String key) {
        String value = properties.getString(key);
        if (value == null) {
            return null;
        }
        return trim(value);
    }

    protected String getPropertyString(Configuration properties, String key, String defaultValue) {
        String value = properties.getString(key, defaultValue);
        if (value == null) {
            return null;
        }
        return trim(value);
    }

    protected String trim(String value) {
        //value = value.replaceFirst("^[\\u0020]+", "");
        //value = value.replaceFirst("[\\u0020]+$", "");
        return value;
    }

    public Config(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    //-- Need Override

    /**
     * Load all properties and recheck again for valid property value.
     * use getProperty() and then set into your member that has the get() function for.
     *
     * @return True is valid, false is invalid then exit the program with an error code.
     */
    protected abstract boolean loadProperties();

    protected abstract void saveProperties() throws ConfigurationException;

    //-- Shared for Write

    protected void setPropertyString(Configuration properties, String key, String defaultValue, String value) {
        if (defaultValue.compareTo(value) == 0) properties.clearProperty(key);
        else properties.setProperty(key, (value == null) ? "" : trim(value));
    }

    protected void setPropertyInt(Configuration properties, String key, int defaultValue, int value) {
        if (defaultValue == value) properties.clearProperty(key);
        else properties.setProperty(key, value);
    }

    protected void setPropertyBoolean(Configuration properties, String key, boolean defaultValue, boolean value) {
        if (defaultValue == value) properties.clearProperty(key);
        else properties.setProperty(key, value);
    }

}
