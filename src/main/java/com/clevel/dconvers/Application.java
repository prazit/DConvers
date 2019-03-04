package com.clevel.dconvers;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.data.*;
import com.clevel.dconvers.input.*;
import com.clevel.dconvers.ngin.*;
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
public class Application extends AppBase {

    public String[] args;
    public Switches switches;

    public DataConversionConfigFile dataConversionConfigFile;

    public HashMap<String, SFTP> sftpMap;
    public HashMap<SystemVariable, DataColumn> systemVariableMap;

    public List<Converter> converterList;
    public Converter currentConverter;
    public DataLong currentState;

    public SummaryTable summaryTable;

    public boolean hasWarning;
    public boolean exitOnError;
    public long errorCode;
    public long warningCode;
    public long successCode;

    public TimeTracker timeTracker;

    private HashMap<String, DataSource> dataSourceMap;
    private Date appStartDate;

    public Application(String[] args) {
        super("DConvers");
        this.args = args;
        loadLogger();

        hasWarning = false;
        exitOnError = false;

        errorCode = Defaults.EXIT_CODE_ERROR.getIntValue();
        warningCode = Defaults.EXIT_CODE_WARNING.getIntValue();
        successCode = Defaults.EXIT_CODE_SUCCESS.getIntValue();

        currentConverter = null;
    }

    @Override
    protected Logger loadLogger() {
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

        return log;
    }

    public void start() {
        timeTracker.start(TimeTrackerKey.APPLICATION, "start to stop");
        appStartDate = new Date();
        this.application = this;

        initSystemVariables();

        switches = new Switches(this);
        if (!switches.isValid()) {
            performInvalidSwitches();
        }

        if (switches.isHelp()) {
            performHelp();
            stop();
        }

        String dataConversionConfigFilename = switches.getSource();
        log.debug("Working directory is '{}'", System.getProperty("user.dir"));
        log.info("DConvers configuration file is '{}'.", dataConversionConfigFilename);

        log.trace("Application. Load DataConversionConfigFile.");
        dataConversionConfigFile = new DataConversionConfigFile(this, dataConversionConfigFilename);
        currentState.setValue((long) dataConversionConfigFile.getSuccessCode());

        if (!dataConversionConfigFile.isValid()) {
            if (dataConversionConfigFile.isChildValid()) {
                performInvalidConfigFile();
            } else {
                performInvalidConfigChild();
            }
        }

        errorCode = dataConversionConfigFile.getErrorCode();
        warningCode = dataConversionConfigFile.getWarningCode();
        successCode = dataConversionConfigFile.getSuccessCode();

        long currentStatus = currentState.getLongValue();
        if (currentStatus == Defaults.EXIT_CODE_SUCCESS.getIntValue()) {
            currentState.setValue(successCode);
        } else if (currentStatus == Defaults.EXIT_CODE_ERROR.getIntValue()) {
            currentState.setValue(errorCode);
        } else {
            currentState.setValue(warningCode);
        }

        boolean success = true;
        exitOnError = dataConversionConfigFile.isExitOnError();
        log.debug("exit on error is '{}'", exitOnError);

        DataLong targetFileNumber = (DataLong) systemVariableMap.get(SystemVariable.TARGET_FILE_NUMBER);
        DataLong mappingFileNumber = (DataLong) systemVariableMap.get(SystemVariable.MAPPING_FILE_NUMBER);
        DataLong sourceFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        targetFileNumber.setValue((long) (dataConversionConfigFile.getTargetFileNumber()) - 1);
        mappingFileNumber.setValue((long) (dataConversionConfigFile.getMappingFileNumber()) - 1);
        sourceFileNumber.setValue((long) (dataConversionConfigFile.getSourceFileNumber()) - 1);

        summaryTable = new SummaryTable(this);
        summaryTable.setOwner(this);

        log.trace("Application. Load DataSources.");
        dataSourceMap = new HashMap<>();
        DataSource dataSource;
        String dataSourceName;
        HashMap<String, DataSourceConfig> dataSourceConfigMap = dataConversionConfigFile.getDataSourceConfigMap();
        if (dataSourceConfigMap != null) {
            for (DataSourceConfig dataSourceConfig : dataSourceConfigMap.values()) {
                dataSourceName = dataSourceConfig.getName();
                if (dataSourceConfig.isEmailDataSource()) {
                    dataSource = new EmailDataSource(this, dataSourceName, dataSourceConfig);
                } else {
                    dataSource = new DataSource(this, dataSourceName, dataSourceConfig);
                }

                if (!dataSource.isValid()) {
                    performInvalidDataSource(dataSource);
                }
                dataSourceMap.put(dataSourceName.toUpperCase(), dataSource);

                dataSource.runPre();
            }
        } else if (exitOnError) {
            stopWithError();
        }

        dataSourceName = Property.SQL.key();
        dataSourceMap.put(dataSourceName.toUpperCase(), new SQLDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));

        dataSourceName = Property.MARKDOWN.key();
        dataSourceMap.put(dataSourceName.toUpperCase(), new MarkdownDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));

        dataSourceName = Property.SYSTEM.key();
        dataSourceMap.put(dataSourceName.toUpperCase(), new SystemDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));

        dataSourceName = Property.RESULT_SET_META_DATA.key();
        dataSourceMap.put(dataSourceName.toUpperCase(), new ResultSetMetaDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));

        dataSourceName = Property.DIR.key();
        dataSourceMap.put(dataSourceName.toUpperCase(), new DirDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));


        log.trace("Application. Load SFTP Services.");
        sftpMap = new HashMap<>();
        SFTP sftp;
        String sftpName;
        HashMap<String, SFTPConfig> sftpConfigMap = dataConversionConfigFile.getSftpConfigMap();
        if (sftpConfigMap != null) {
            for (SFTPConfig sftpConfig : sftpConfigMap.values()) {
                sftpName = sftpConfig.getName();

                sftp = new SFTP(this, sftpName, sftpConfig);
                if (!sftp.isValid()) {
                    performInvalidSFTP(sftp);
                }
                sftpMap.put(sftpName.toUpperCase(), sftp);
            }
        } else if (exitOnError) {
            stopWithError();
        }

        log.trace("Application. Load Converters.");
        converterList = new ArrayList<>();
        Converter converter;
        String converterName;
        HashMap<String, ConverterConfigFile> converterConfigMap = dataConversionConfigFile.getConverterConfigMap();
        if (converterConfigMap != null) {
            for (ConverterConfigFile converterConfigFile : converterConfigMap.values()) {
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
        } else if (exitOnError) {
            stopWithError();
        }

        log.trace("Application. Launch Converters to transfer, transform and create output.");
        converterList.sort((o1, o2) -> o1.getConverterConfigFile().getIndex() > o2.getConverterConfigFile().getIndex() ? 1 : -1);

        Converter lastConverter = null;
        if (converterList.size() > 0) {
            lastConverter = converterList.get(converterList.size() - 1);

            for (Converter convert : converterList) {
                log.info("Converter({}) configuration file is '{}'", convert.getConverterConfigFile().getIndex(), convert.getName());
                currentConverter = convert;

                success = convert.printSources() && success;

                success = convert.buildTargets() && success;
                success = convert.printTarget() && success;

                if (!lastConverter.equals(convert)) {
                    convert.close();
                }
            }
        }

        if (!success && exitOnError) {
            stopWithError();
        }

        log.trace("Application. Run post process of each datasource.");
        for (DataSource dataSourceItem : dataSourceMap.values()) {
            dataSourceItem.runPost();
        }

        log.trace("Application. print summary table.");
        summaryTable.sort();
        summaryTable.print(dataConversionConfigFile.getSummaryOutputConfig());

        currentConverter = null;
        if (lastConverter != null) {
            lastConverter.close();
        }

        // Have some errors
        if (currentState.getLongValue() == dataConversionConfigFile.getErrorCode()) {
            stopWithError();
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

        systemVariableMap.put(SystemVariable.NOW, new ComputeNow(this, "NOW"));
        systemVariableMap.get(SystemVariable.EMPTY_STRING).setValue("");
        ((DataDate) systemVariableMap.get(SystemVariable.APPLICATION_START)).setValue(appStartDate);

        currentState = (DataLong) systemVariableMap.get(SystemVariable.APPLICATION_STATE);
        currentState.setValue((long) Defaults.EXIT_CODE_SUCCESS.getIntValue());
    }

    private void performTimeTracker() {
        timeTracker.stop(TimeTrackerKey.APPLICATION);
        log.debug(timeTracker.toString());
    }

    public void stop() {
        log.trace("Application.stop.");

        closeAllSFTP();
        closeAllDataSource();
        performTimeTracker();

        log.info("SUCCESS");
        System.exit(dataConversionConfigFile.getSuccessCode());
    }

    public void stopWithError() {
        log.trace("Application.stopWithError.");

        closeAllSFTP();
        closeAllDataSource();
        performTimeTracker();

        log.info("EXIT WITH SOME ERROR");
        System.exit(dataConversionConfigFile.getErrorCode());
    }

    public void stopWithWarning() {
        log.trace("Application.stopWithWarning.");

        closeAllSFTP();
        closeAllDataSource();
        performTimeTracker();

        log.info("SUCCESSFUL WITH WARNING");
        System.exit(dataConversionConfigFile.getWarningCode());
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

        error("Invalid CLI Switches({}) please see help below", switches);
        log.debug("invalid switches: {}", switches);

        performHelp();
        stopWithError();
    }

    private void performInvalidConfigFile() {
        log.trace("Application.performInvalidConfigFile.");

        error("Invalid Configuration File({}) please check the file or see readme.md", switches.getSource());
        log.debug("dataConversionConfigFile = {}", dataConversionConfigFile);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidConfigChild() {
        log.trace("Application.performInvalidConfigChild.");

        error("Invalid Child of Configuration File({}) please check the file or see readme.md", switches.getSource());
        log.debug("dataConversionConfigFile = {}", dataConversionConfigFile);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidDataSource(DataSource dataSource) {
        log.trace("Application.performInvalidDataSource.");

        error("Invalid Datasource ({}) please check {}.", dataSource.getName(), dataConversionConfigFile.getName());
        log.debug("datasource = {}", dataSource);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidSFTP(SFTP sftp) {
        log.trace("Application.performInvalidSFTP.");

        error("Invalid SFTP({}) please check {}.", sftp.getName(), dataConversionConfigFile.getName());
        log.debug("sftp = {}", sftp);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidConverter(Converter converter) {
        log.trace("Application.performInvalidConverter.");

        error("Invalid Converter ({}) please check the configuration files or see readme.md", converter.getName());
        log.debug("converter = {}", converter);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performHelp() {
        log.trace("Application.performHelp.");

        String syntax = "dconvers [switches]\n" +
                "\nSwitches:\n";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(syntax, switches.getOptions());
    }

    //======= Utilities =======

    private HashMap<SystemVariable, DataColumn> createSystemVariableMap() {
        List<SystemVariable> systemVariableList = Arrays.asList(SystemVariable.values());
        HashMap<SystemVariable, DataColumn> variables = new HashMap<>();
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

    public DataSource getDataSource(String dataSourceName) {
        if (dataSourceName == null) {
            return null;
        }

        return dataSourceMap.get(dataSourceName.toUpperCase());
    }

    public DataSource getDataSource(DataSourceConfig dataSourceConfig) {
        DataSourceConfig config;
        for (DataSource dataSource : dataSourceMap.values()) {
            config = dataSource.getDataSourceConfig();
            if (config.equals(dataSourceConfig)) {
                return dataSource;
            }
        }
        return null;
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
        if (value != null && value.equalsIgnoreCase("NULL")) {
            value = null;
        }

        boolean isBigInteger = false;
        switch (columnType) {
            case Types.BIGINT:
            case Types.NUMERIC:
                isBigInteger = true;
                /* remember: don't put break; here*/

            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.BOOLEAN:
            case Types.BIT:
                int type = isBigInteger ? Types.BIGINT : Types.INTEGER;
                Long longValue = null;
                if (value != null) {
                    longValue = toLong(value);
                    if (longValue == null) {
                        error("Invalid value({}) for integer column({})", value, columnName);
                    }
                }
                dataColumn = new DataLong(this, 0, type, columnName, longValue);
                break;

            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                Double doubleValue = null;
                if (value != null) {
                    doubleValue = toDouble(value);
                    if (doubleValue == null) {
                        error("Invalid value({}) for decimal column({})", value, columnName);
                    }
                }
                dataColumn = new DataBigDecimal(this, 0, Types.DECIMAL, columnName, BigDecimal.valueOf(doubleValue));
                break;

            case Types.DATE:
            case Types.TIMESTAMP:
                dataColumn = new DataDate(this, 0, Types.DATE, columnName, value);
                break;

            /*case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                dataColumn = new DataString(this, 0, Types.VARCHAR, columnName, value);
                break;*/
            default:
                dataColumn = new DataString(this, 0, Types.VARCHAR, columnName, value);
        }

        //log.debug("createDataColumn(columnName:{}, valueAsString:{}) = {}", columnName, value == null ? "null" : "\"" + value + "\"", dataColumn);
        return dataColumn;
    }

    private Long toLong(String value) {
        Long longValue;
        try {
            longValue = Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
        return longValue;
    }

    private Double toDouble(String value) {
        Double doubleValue;
        try {
            doubleValue = Double.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
        return doubleValue;
    }

}
