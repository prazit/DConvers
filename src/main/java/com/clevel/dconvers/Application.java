package com.clevel.dconvers;

import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.*;
import com.clevel.dconvers.ngin.data.*;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.util.*;

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
    public List<Converter> converterList;
    public Map<SystemVariable, DataColumn> systemVariableMap;

    public DataTable reportTable;

    public boolean hasWarning;

    public Application(String[] args) {
        log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        this.args = args;

        reportTable = new DataTable("Report", "id");
        hasWarning = false;

        systemVariableMap = createSystemVariableMap();

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
            if (dataConversionConfigFile.isChildValid()) {
                performInvalidSource();
            } else {
                stopWithError();
            }
        }

        DataLong targetFileNumber = (DataLong) systemVariableMap.get(SystemVariable.TARGET_FILE_NUMBER);
        DataLong mappingFileNumber = (DataLong) systemVariableMap.get(SystemVariable.MAPPING_FILE_NUMBER);
        DataLong sourceFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        DataLong reportFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        targetFileNumber.setValue(dataConversionConfigFile.getTargetFileNumber() - 1);
        mappingFileNumber.setValue(dataConversionConfigFile.getMappingFileNumber() - 1);
        sourceFileNumber.setValue(dataConversionConfigFile.getSourceFileNumber() - 1);
        reportFileNumber.setValue(dataConversionConfigFile.getReportFileNumber() - 1);

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

            dataSource.runPre();
            dataSource.generateConverterFile();
        }

        dataSourceName = Property.SQL.key();
        dataSourceMap.put(dataSourceName, new SQLSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));

        log.trace("Application. Load Converters.");
        converterList = new ArrayList<>();
        Converter converter;
        String converterName;
        for (ConverterConfigFile converterConfigFile : dataConversionConfigFile.getConverterConfigMap().values()) {
            converterName = converterConfigFile.getName();
            converter = new Converter(this, converterName, converterConfigFile);

            if (!converter.isValid()) {
                performInvalidConverter(converter);
            }

            converterList.add(converter);
        }
        log.info("Has {} converters", converterList.size());

        log.trace("Application. Launch Converters.");
        converterList.sort((o1, o2) -> o1.getConverterConfigFile().getIndex() > o2.getConverterConfigFile().getIndex() ? 1 : -1);

        boolean printSource = !dataConversionConfigFile.getOutputSourcePath().isEmpty();
        boolean printTarget = !dataConversionConfigFile.getOutputTargetPath().isEmpty();
        boolean printMapping = !dataConversionConfigFile.getOutputMappingPath().isEmpty();
        boolean success = false;
        for (Converter convert : converterList) {
            success = convert.convert();
            success = success && convert.print(printSource, printTarget, printMapping);
        }
        if (!success) {
            stopWithError();
        }

        // TODO Print Report Table

        log.trace("Application. Run post process of each datasource.");
        for (DataSource dataSourceItem : dataSourceMap.values()) {
            dataSourceItem.runPost();
        }

        // Successful with warning
        if (hasWarning) {
            stopWithWarning();
        }

        // Successful without warning
        stop();
    }

    public void stop() {
        closeAllDataSource();
        log.info("SUCCESS");
        System.exit(0);
    }

    public void stopWithError() {
        closeAllDataSource();
        log.info("HAS SOME ERROR");
        System.exit(1);
    }

    public void stopWithWarning() {
        closeAllDataSource();
        log.info("SUCCESS WITH WARNING");
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

        String syntax = "dconvers [switches]\n" +
                "\nSwitches:\n";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(syntax, switches.getOptions());
    }

    //==== Utilities ====

    private Map<SystemVariable, DataColumn> createSystemVariableMap() {
        List<SystemVariable> systemVariableList = Arrays.asList(SystemVariable.values());
        Map<SystemVariable, DataColumn> variables = new HashMap<>();
        DataColumn dataColumn;

        for (SystemVariable systemVariable : systemVariableList) {
            dataColumn = createDataColumn(systemVariable.name(), systemVariable.getDataType(), null);
            variables.put(systemVariable, dataColumn);
        }

        return variables;
    }

    /**
     * Create DataColumn by Type
     *
     * @param columnName initial name
     * @param columnType see java.sql.Types for detailed
     * @return new instance of DataColumn depend on columnType
     */
    public DataColumn createDataColumn(String columnName, int columnType, String value) {
        DataColumn dataColumn;

        switch (columnType) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.BOOLEAN:
            case Types.BIGINT:
            case Types.BIT:
            case Types.NUMERIC:
                dataColumn = new DataLong(0, columnType, columnName, value == null ? 0 : Long.valueOf(value));
                break;

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                dataColumn = new DataString(0, columnType, columnName, value);
                break;

            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                dataColumn = new DataBigDecimal(0, columnType, columnName, value == null ? null : BigDecimal.valueOf(Double.valueOf(value)));
                break;

            case Types.DATE:
            case Types.TIMESTAMP:
                dataColumn = new DataDate(0, columnType, columnName, value == null ? null : Date.valueOf(value));
                break;

            default:
                dataColumn = new DataString(0, columnType, columnName, value);
        }

        return dataColumn;
    }
}
