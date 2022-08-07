package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.LibraryMode;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.JSONConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ConfigFile extends Config {

    protected FileBasedConfigurationBuilder<FileBasedConfiguration> propertiesBuilder;

    public ConfigFile(DConvers dconvers, String configFile) {
        super(dconvers, configFile);

        if (this instanceof VersionConfigFile) createProperties(configFile);
        else switch (dconvers.switches.getSourceType()) {
            case XML:
                createXMLProperties(configFile);
                break;

            case JSON:
                createJSONProperties(configFile);
                break;

            default: //case PROPERTIES:
                createProperties(configFile);
        }

        loadDefaults();
        if (LibraryMode.MANUAL != dconvers.switches.getLibraryMode()) {
            if (valid) valid = loadProperties();
            if (valid) valid = validate();
        }
    }

    private void createJSONProperties(String configFile) {
        Parameters params = new Parameters();
        try {
            propertiesBuilder = new FileBasedConfigurationBuilder<>(JSONConfiguration.class);
            propertiesBuilder.configure(params.fileBased().setFileName(configFile));
            properties = propertiesBuilder.getConfiguration();
            valid = true;
            log.trace("load json-properties is successful ({})", configFile);
        } catch (NoClassDefFoundError nc) {
            if (!(this instanceof VersionConfigFile)) error("Load json-properties '{}' is failed! NoClassDefFoundError: {}", configFile, nc.getMessage());
            properties = new PropertiesConfiguration();
            valid = false;

            if (log.isDebugEnabled()) {
                StringWriter errors = new StringWriter();
                nc.printStackTrace(new PrintWriter(errors));
                log.debug(nc.getMessage(), errors.toString());
            }
        } catch (Exception e) {
            if (!(this instanceof VersionConfigFile)) {
                error("Load json-properties '{}' is failed! {}: {}", configFile, e.getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void createXMLProperties(String configFile) {
        Parameters params = new Parameters();
        try {
            propertiesBuilder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);
            propertiesBuilder.configure(params.xml().setFileName(configFile));
            properties = propertiesBuilder.getConfiguration();
            valid = true;
            log.trace("load xml-properties is successful ({})", configFile);
        } catch (NoClassDefFoundError nc) {
            if (!(this instanceof VersionConfigFile)) error("Load xml-properties '{}' is failed! NoClassDefFoundError: {}", configFile, nc.getMessage());
            properties = new PropertiesConfiguration();
            valid = false;

            if (log.isDebugEnabled()) {
                StringWriter errors = new StringWriter();
                nc.printStackTrace(new PrintWriter(errors));
                log.debug(nc.getMessage(), errors.toString());
            }
        } catch (Exception e) {
            if (!(this instanceof VersionConfigFile)) error("Load xml-properties '{}' is failed! {}: {}", configFile, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void createProperties(String configFile) {
        Parameters params = new Parameters();
        try {
            propertiesBuilder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
            propertiesBuilder.configure(params.properties().setFileName(configFile));
            properties = propertiesBuilder.getConfiguration();

            valid = true;
            log.trace("load properties is successful ({})", configFile);
        } catch (NoClassDefFoundError nc) {
            if (!(this instanceof VersionConfigFile)) error("Load properties '{}' is failed!", configFile);
            properties = new PropertiesConfiguration();
            valid = false;

            if (log.isDebugEnabled()) {
                StringWriter errors = new StringWriter();
                nc.printStackTrace(new PrintWriter(errors));
                log.debug(nc.getMessage(), errors.toString());
            }
        } catch (Exception e) {
            if (!(this instanceof VersionConfigFile)) error("Load properties '{}' is failed! {}", configFile, e.getMessage());
        }
    }

    protected abstract void saveProperties(OutputStream outputStream) throws ConfigurationException;

}
