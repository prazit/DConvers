package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.LibraryMode;
import org.apache.commons.configuration2.Configuration;

/**
 * OutputPluginConfig need to implement two methods loadProperties() and loadLogger()
 */
public abstract class OutputPluginConfig extends Config{

    public OutputPluginConfig(DConvers dconvers, String baseProperty) {
        super(dconvers, baseProperty);
        log.trace("OutputPluginConfig({}) is created", name);
    }

    public void loadConfig(Configuration baseProperties) {
        this.properties = baseProperties;

        loadDefaults();
        if(LibraryMode.MANUAL != dconvers.switches.getLibraryMode()) {
            valid = loadProperties();
            if (valid) valid = validate();
        }
    }

}
