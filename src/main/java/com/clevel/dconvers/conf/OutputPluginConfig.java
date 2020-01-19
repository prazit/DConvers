package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.configuration2.Configuration;

/**
 * OutputPluginConfig need to implement two methods loadProperties() and loadLogger()
 */
public abstract class OutputPluginConfig extends Config{

    public OutputPluginConfig(Application application, String baseProperty) {
        super(application, baseProperty);
        log.trace("OutputPluginConfig({}) is created", name);
    }

    public void loadConfig(Configuration baseProperties) {
        this.properties = baseProperties;
        valid = loadProperties();
        if (valid) valid = validate();
    }

}
