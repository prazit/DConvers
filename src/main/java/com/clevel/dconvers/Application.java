package com.clevel.dconvers;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.SFTP;
import com.clevel.dconvers.ngin.data.*;
import com.clevel.dconvers.ngin.input.DataSource;
import com.clevel.dconvers.ngin.input.EmailDataSource;
import com.clevel.dconvers.ngin.input.MarkdownDataSource;
import com.clevel.dconvers.ngin.input.SQLDataSource;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
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
    public Map<String, SFTP> sftpMap;
    public Map<SystemVariable, DataColumn> systemVariableMap;

    public List<Converter> converterList;
    public Converter currentConverter;

    public DataTable reportTable;

    public DataString errorMessages;
    public DataString warningMessages;
    public DataString progressMessages;
    public boolean hasWarning;

    public Application(String[] args) {
        this.args = args;
        loadLogger();

        reportTable = new DataTable("Report", "id");
        hasWarning = false;

        currentConverter = null;
    }

    private void loadLogger() {
        for (String arg : this.args) {
            if (arg.startsWith("--logback")) {
                String logback = arg.substring(arg.indexOf("=") + 1).replaceAll("[\"]", "");
                System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, logback);
                break;
            }
        }

        log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) log).getLoggerContext();
        URL url = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);
        log.info("Logback configuration file is '{}'.", url);
    }

    public void start() {
        initSystemVariables();

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
                performInvalidConfigFile();
            } else {
                stopWithError();
            }
        }

        boolean success = true;
        boolean exitOnError = dataConversionConfigFile.isExitOnError();
        log.debug("exit on error is '{}'", exitOnError);

        DataLong targetFileNumber = (DataLong) systemVariableMap.get(SystemVariable.TARGET_FILE_NUMBER);
        DataLong mappingFileNumber = (DataLong) systemVariableMap.get(SystemVariable.MAPPING_FILE_NUMBER);
        DataLong sourceFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        DataLong reportFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        targetFileNumber.setValue((long) (dataConversionConfigFile.getTargetFileNumber()));
        mappingFileNumber.setValue((long) (dataConversionConfigFile.getMappingFileNumber()));
        sourceFileNumber.setValue((long) (dataConversionConfigFile.getSourceFileNumber()));
        reportFileNumber.setValue((long) (dataConversionConfigFile.getReportFileNumber()));

        log.trace("Application. Load DataSources.");
        dataSourceMap = new HashMap<>();
        DataSource dataSource;
        String dataSourceName;
        for (DataSourceConfig dataSourceConfig : dataConversionConfigFile.getDataSourceConfigMap().values()) {
            dataSourceName = dataSourceConfig.getName();
            if (dataSourceConfig.isEmailDataSource()) {
                dataSource = new EmailDataSource(this, dataSourceName, dataSourceConfig);
            } else {
                dataSource = new DataSource(this, dataSourceName, dataSourceConfig);
            }

            if (!dataSource.isValid()) {
                performInvalidDataSource(dataSource);
            }
            dataSourceMap.put(dataSourceName, dataSource);

            dataSource.runPre();
        }

        dataSourceName = Property.SQL.key();
        dataSourceMap.put(dataSourceName, new SQLDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));

        dataSourceName = Property.MARKDOWN.key();
        dataSourceMap.put(dataSourceName, new MarkdownDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));

        log.trace("Application. Load SFTP Services.");
        sftpMap = new HashMap<>();
        SFTP sftp;
        String sftpName;
        for (SFTPConfig sftpConfig : dataConversionConfigFile.getSftpConfigMap().values()) {
            sftpName = sftpConfig.getName();

            sftp = new SFTP(this, sftpName, sftpConfig);
            if (!sftp.isValid()) {
                performInvalidSFTP(sftp);
            }
            sftpMap.put(sftpName, sftp);
        }

        log.trace("Application. Load Converters.");
        converterList = new ArrayList<>();
        Converter converter;
        String converterName;
        for (ConverterConfigFile converterConfigFile : dataConversionConfigFile.getConverterConfigMap().values()) {
            converterName = converterConfigFile.getName();
            converter = new Converter(this, converterName, converterConfigFile);

            if (!converter.isValid()) {
                if (exitOnError) {
                    performInvalidConverter(converter);
                }
                success = false;
            }

            converterList.add(converter);
        }
        log.info("Has {} converter(s)", converterList.size());

        log.trace("Application. Launch Converters.");
        converterList.sort((o1, o2) -> o1.getConverterConfigFile().getIndex() > o2.getConverterConfigFile().getIndex() ? 1 : -1);

        if (converterList.size() > 0) {
            for (Converter convert : converterList) {
                currentConverter = convert;
                success = success && convert.convert();
                success = success && convert.print();
            }
        }
        currentConverter = null;

        if (!success) {
            stopWithError();
        }

        // TODO Print Summary Report Table

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

    private void initSystemVariables() {
        systemVariableMap = createSystemVariableMap();

        errorMessages = (DataString) systemVariableMap.get(SystemVariable.ERROR_MESSAGES);
        warningMessages = (DataString) systemVariableMap.get(SystemVariable.WARNING_MESSAGES);
        progressMessages = (DataString) systemVariableMap.get(SystemVariable.PROGRESS_MESSAGES);
        errorMessages.setValue("");
        warningMessages.setValue("");
        progressMessages.setValue("");

        DataString emptyString = (DataString) systemVariableMap.get(SystemVariable.EMPTY_STRING);
        emptyString.setValue("");

        DataDate now = (DataDate) systemVariableMap.get(SystemVariable.NOW);
        now.setValue(new Date());
    }

    public void stop() {
        log.trace("Application.stop.");

        closeAllSFTP();
        closeAllDataSource();

        log.info("SUCCESS\n\n");
        System.exit(0);
    }

    public void stopWithError() {
        log.trace("Application.stopWithError.");

        closeAllSFTP();
        closeAllDataSource();

        log.info("EXIT WITH SOME ERROR\n\n");
        System.exit(1);
    }

    public void stopWithWarning() {
        log.trace("Application.stopWithWarning.");

        closeAllSFTP();
        closeAllDataSource();

        log.info("SUCCESSFUL WITH WARNING\n\n");
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

    private void closeAllSFTP() {
        log.trace("Application.closeAllSFTP.");

        if (sftpMap == null) {
            return;
        }

        for (SFTP sftp : sftpMap.values()) {
            sftp.close();
        }
    }

    private void performInvalidSwitches() {
        log.trace("Application.performInvalidSwitches.");

        log.error("Invalid CLI Switches({}) please see help below", switches);
        log.debug("invalid switches: {}", switches);
        performHelp();
        stopWithError();
    }

    private void performInvalidConfigFile() {
        log.trace("Application.performInvalidConfigFile.");

        log.error("Invalid Configuration File({}) please check the file or see readme.md", switches.getSource());
        log.debug("source = {}", dataConversionConfigFile);
        stopWithError();
    }

    private void performInvalidDataSource(DataSource dataSource) {
        log.trace("Application.performInvalidDataSource.");

        log.error("Invalid Datasource ({}) please check {}.", dataSource.getName(), dataConversionConfigFile.getName());
        log.debug("datasource = {}", dataSource);
        stopWithError();
    }

    private void performInvalidSFTP(SFTP sftp) {
        log.trace("Application.performInvalidSFTP.");

        log.error("Invalid SFTP({}) please check {}.", sftp.getName(), dataConversionConfigFile.getName());
        log.debug("sftp = {}", sftp);
        stopWithError();
    }

    private void performInvalidConverter(Converter converter) {
        log.trace("Application.performInvalidConverter.");

        log.error("Invalid Converter ({}) please check the configuration files or see readme.md", converter.getName());
        log.debug("converter = {}", converter);
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

    public String getSystemVariableValue(SystemVariable systemVariable) {
        DataColumn dataColumn = systemVariableMap.get(systemVariable);
        if (dataColumn == null) {
            return "";
        }

        return dataColumn.getValue();
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
                dataColumn = new DataLong(0, columnType, columnName, value == null ? null : Long.valueOf(value));
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
                dataColumn = new DataDate(0, columnType, columnName, value);
                break;

            default:
                dataColumn = new DataString(0, columnType, columnName, value);
        }

        return dataColumn;
    }

}
