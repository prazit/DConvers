package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.AppBase;
import org.apache.commons.configuration2.Configuration;

public abstract class Config extends AppBase {

    //-- Shared Objects

    protected Configuration properties;

    public Config(Application application, String name) {
        super(application, name);
    }

    //-- Need Override

    /**
     * Load all properties and recheck again for valid property value.
     * use getProperty() and then set into your member that has the get() function for.
     *
     * @return True is valid, false is invalid then exit the program with an error code.
     */
    protected abstract boolean loadProperties();

    //-- access read only properties

    public Configuration getProperties() {
        return properties;
    }


}
