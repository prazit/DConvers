package com.clevel.dconvers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.clevel.dconvers.conf.ConverterConfigFile;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.DataSource;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application is Global Object that pass through the process from start to the end.
 * <p>
 * This class contains only the public members to let all process can access them directly.
 */
public class Application {

    public String[] args;
    public Logger log;
    public Switches switches;

    public DataConversionConfigFile dataConversionConfigFile;
    public Map<String, DataSource> dataSourceMap;
    public Map<String, Converter> converterMap;

    public Application(String[] args) {
        log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        this.args = args;

        log.trace("Application is created.");
    }

    public void start() {
        switches = new Switches(this);
        if (!switches.isValid()) {
            performInvalidSwitches();
        }

        if (switches.isHelp()) {
            performHelp();
            stop();
        }

        log.trace("Application. Load DataConversionConfigFile.");
        dataConversionConfigFile = new DataConversionConfigFile(this, switches.getSource());
        if (!dataConversionConfigFile.isValid()) {
            performInvalidSource();
        }

        log.trace("Application. Load DataSources.");
        dataSourceMap = new HashMap<>();
        DataSource dataSource;
        String dataSourceName;
        for (DataSourceConfig dataSourceConfig : dataConversionConfigFile.getDataSourceConfigMap().values()) {
            dataSourceName = dataSourceConfig.getName();
            dataSource = new DataSource(this, dataSourceName, dataSourceConfig);

            if (!dataSource.isValid()) {
                performInvalidDataSource(dataSource);
            }

            dataSourceMap.put(dataSourceName, dataSource);
        }

        log.trace("Application. Load Converters.");
        converterMap = new HashMap<>();
        Converter converter;
        String converterName;
        for (ConverterConfigFile converterConfigFile : dataConversionConfigFile.getConverterConfigMap().values()) {
            converterName = converterConfigFile.getName();
            converter = new Converter(this, converterName, converterConfigFile);

            if (!converter.isValid()) {
                performInvalidConverter(converter);
            }

            converterMap.put(converterName, converter);
        }

        log.trace("Application. Launch Converters.");

        for (Converter convert:converterMap.values()) {
            if (!convert.convert()) {
                stopWithError();
            }

            if (!convert.render()) {
                stopWithError();
            }
        }

        // Successful
        stop();
    }

    public void stop() {
        closeAllDataSource();
        log.trace("program exit normally");
        System.exit(0);
    }

    public void stopWithError() {
        closeAllDataSource();
        log.trace("program exit with error");
        System.exit(1);
    }

    public void stopWithWarning() {
        closeAllDataSource();
        log.trace("program exit with warning");
        System.exit(2);
    }

    private void closeAllDataSource() {
        log.trace("Application.closeAllDataSource.");

        if (dataSourceMap == null) {
            return;
        }

        for (DataSource dataSource : dataSourceMap.values()) {
            dataSource.close();
        }
    }

    private void performInvalidSwitches() {
        log.trace("Application.performInvalidSwitches.");
        log.error("Invalid CLI Switches ({}) Please see help below", switches);
        log.debug("invalid switches: {}", switches);
        performHelp();
        stopWithError();
    }

    private void performInvalidSource() {
        log.trace("Application.performInvalidSource.");
        log.error("Invalid Source File ({}) Please see 'sample-conversion.conf' for detailed", switches.getSource());
        log.debug("source = {}", dataConversionConfigFile);
        stopWithError();
    }

    private void performInvalidDataSource(DataSource dataSource) {
        log.trace("Application.performInvalidDataSource.");
        log.error("Invalid Datasource ({}) Please see 'sample-conversion.conf' for detailed", dataSource);
        log.debug("datasource = {}", dataSource);
        stopWithError();
    }

    private void performInvalidConverter(Converter converter) {
        log.trace("Application.performInvalidConverter.");
        log.error("Invalid Converter ({}) Please see 'sample-converter.conf' for detailed", converter);
        log.debug("converter = {}", dataConversionConfigFile.toString());
        stopWithError();
    }

    private void performHelp() {
        log.trace("Application.performHelp.");

        String syntax = "dconvers --source=<file> [switches]\n" +
                "\nSwitches:\n";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(syntax, switches.getOptions());
    }

}
