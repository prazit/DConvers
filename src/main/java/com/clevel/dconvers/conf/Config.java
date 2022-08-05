package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.ngin.AppBase;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;

public abstract class Config extends AppBase {

    //-- Shared for Read

    protected Configuration properties;

    public Configuration getProperties() {
        return properties;
    }

    public PropertiesConfigurationLayout getPropertiesLayout() {
        if (properties instanceof PropertiesConfiguration) {
            return ((PropertiesConfiguration) properties).getLayout();
        }

        error("getPropertiesLayout: {} doesn't has layout!");
        return null;
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
     * Set all properties by default values before load from property file.
     */
    protected abstract void loadDefaults();

    /**
     * Load all properties and recheck again for valid property value.
     * use getProperty() and then set into your member that has the get() function for.
     *
     * @return True is valid, false is invalid then exit the program with an error code.
     */
    protected abstract boolean loadProperties();

    protected abstract void saveProperties() throws ConfigurationException;

    //-- Shared for Write

    protected void addPropertyString(Configuration properties, String key, String defaultValue, String value) {
        if (defaultValue == null) defaultValue = "";
        if (value == null) value = defaultValue;
        properties.addProperty(key, trim(value));
    }

    protected void setPropertyString(Configuration properties, String key, String defaultValue, String value) {
        if (defaultValue == null) defaultValue = "";
        else defaultValue = trim(defaultValue);
        if (value == null) value = "";
        else value = trim(value);
        if (!dconvers.switches.isSaveDefaultValue() && defaultValue.compareTo(value) == 0) properties.clearProperty(key);
        else properties.setProperty(key, value);
    }

    protected void setPropertyInt(Configuration properties, String key, int defaultValue, int value) {
        if (!dconvers.switches.isSaveDefaultValue() && defaultValue == value) properties.clearProperty(key);
        else properties.setProperty(key, value);
    }

    protected void setPropertyBoolean(Configuration properties, String key, boolean defaultValue, boolean value) {
        if (!dconvers.switches.isSaveDefaultValue() && defaultValue == value) properties.clearProperty(key);
        else properties.setProperty(key, value);
    }

}
