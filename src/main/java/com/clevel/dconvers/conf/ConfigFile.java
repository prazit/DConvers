package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ConfigFile extends Config {

    protected FileBasedConfigurationBuilder<FileBasedConfiguration> propertiesBuilder;

    public ConfigFile(DConvers dconvers, String configFile) {
        super(dconvers, configFile);

        Parameters params = new Parameters();

        try {
            propertiesBuilder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
            propertiesBuilder.configure(params.properties().setFileName(configFile));
            properties = propertiesBuilder.getConfiguration();
            valid = true;
            log.trace("load properties is successful ({})", configFile);
        } catch (NoClassDefFoundError nc) {
            error("Load properties '{}' is failed!", configFile);
            properties = new PropertiesConfiguration();
            valid = false;

            if (log.isDebugEnabled()) {
                StringWriter errors = new StringWriter();
                nc.printStackTrace(new PrintWriter(errors));
                log.debug(nc.getMessage(), errors.toString());
            }
        } catch (Exception e) {
            error("Load properties '{}' is failed! {}", configFile, e.getMessage());
        }

        if (!dconvers.getManualMode()) {
            if (valid) valid = loadProperties();
            if (valid) valid = validate();
        }
    }

}
