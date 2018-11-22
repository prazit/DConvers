package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ConfigFile extends Config {

    public ConfigFile(Application application, String configFile) {
        super(application, configFile);

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder;

        try {
            builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
            builder.configure(params.properties().setFileName(configFile));
            properties = builder.getConfiguration();
            valid = true;
            log.trace("load properties is successful ({})", configFile);
        } catch (NoClassDefFoundError nc) {
            log.error("Load properties '{}' is failed!", configFile);

            if (log.isDebugEnabled()) {
                StringWriter errors = new StringWriter();
                nc.printStackTrace(new PrintWriter(errors));
                log.debug(nc.getMessage(), errors.toString());
            }
        } catch (Exception e) {
            log.error("Load properties '{}' is failed! {}", configFile, e.getMessage());
        }

        if (valid) valid = loadProperties();
        if (valid) valid = validate();
    }

}
