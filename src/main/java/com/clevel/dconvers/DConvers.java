package com.clevel.dconvers;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.clevel.dconvers.calc.CalcTypes;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.data.*;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.format.VersionFormatter;
import com.clevel.dconvers.input.*;
import com.clevel.dconvers.ngin.*;
import com.clevel.dconvers.output.OutputTypes;
import com.clevel.dconvers.ngin.Pair;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.Types;
import java.util.*;

/**
 * DConvers is Global Object that pass through the process from start to the end.
 * <p>
 * This class contains only the public members to let all process can access them directly.
 */
public class DConvers extends AppBase {

    public String[] args;
    public Switches switches;

    /*Notice: config-PRESET and predefined Readers used by Library Modes*/
    public DataConversionConfigFile dataConversionConfigFile;
    public HashMap<String, Reader> readerMap;

    public HashMap<String, SFTP> sftpMap;
    public HashMap<String, SMTP> smtpMap;
    public HashMap<SystemVariable, DataColumn> systemVariableMap;
    public HashMap<String, DataColumn> userVariableMap;

    public List<Converter> converterList;
    public Converter currentConverter;
    public DataLong currentState;
    public DataString currentStateMessage;

    public HashMap<String, DataTable> systemTableMap;
    public SummaryTable tableSummary;
    public SummaryTable outputSummary;

    public List<Throwable> errorList;
    public boolean hasWarning;
    public boolean exitOnError;
    public long errorCode;
    public long warningCode;
    public long successCode;

    public TimeTracker timeTracker;

    private HashMap<String, DataSource> dataSourceMap;
    private Date appStartDate;

    private boolean isLibEnd;

    /**
     * run from normal configuration files specified by command-line args.
     */
    public DConvers(String[] args) {
        super("DConvers");

        /* args array need somethings like this
        --source=ETLJOB\config\JOBSTATUS.conf
        --logback=C:\Users\prazi\Documents\LHBank\ETL\DConvers\ETLJOB\config\shared-Local\logback-replace.xml
        --verbose
        --level=TRACE         */
        this.args = Arrays.copyOf(args, args.length);
        timeTracker = new TimeTracker();

        loadSwitches();

        loadLogger();
        log.debug("Switches = {}", switches.toString());

        initSystemVariables();

        isLibEnd = false;

        hasWarning = false;
        exitOnError = false;
        errorList = new ArrayList<>();

        errorCode = Defaults.EXIT_CODE_ERROR.getIntValue();
        warningCode = Defaults.EXIT_CODE_WARNING.getIntValue();
        successCode = Defaults.EXIT_CODE_SUCCESS.getIntValue();

        currentConverter = null;

        dataSourceMap = new HashMap<>();
    }

    private void loadSwitches() {
        switches = new Switches(args);

        switches.postConstruct();

        if (!switches.isValid()) {
            performInvalidSwitches();
        }

        if (switches.getLibraryMode() == LibraryMode.PRESET || switches.getLibraryMode() == LibraryMode.MANUAL) {
            dataConversionConfigFile = new DataConversionConfigFile(this, switches.getSource());
            readerMap = new HashMap<>();
        }
    }

    @Override
    protected Logger loadLogger() {
        if (switches.isLibrary()) {
            log = LoggerFactory.getLogger(getClass());
            return log;
        }

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
        log.debug("Logback: {}", url);

        return log;
    }

    public void start() {
        /*when start from the main class, the timeTracker is already assigned otherwise is not*/
        timeTracker.start(TimeTrackerKey.APPLICATION, "start to stop");
        appStartDate = new Date();
        this.dconvers = this;

        if (switches.isHelp()) {
            performHelp();
            stop();
        }

        if (switches.isVersion()) {
            performVersion();
            stop();
        }

        String dataConversionConfigFilename = LibraryMode.PRESET == switches.getLibraryMode() ? "dataConversionConfigFile.getProperties()" : LibraryMode.MANUAL == switches.getLibraryMode() ? "no-config" : switches.getSource();
        log.debug("Working directory is '{}'", System.getProperty("user.dir"));

        log.info("Engine: {}", systemVariableMap.get(SystemVariable.APPLICATION_FULL_VERSION).getValue());
        log.info("Library Mode: {}", switches.isLibrary() ? switches.getLibraryMode() : "disabled");

        DataColumn configurationVersion = systemVariableMap.get(SystemVariable.CONFIG_VERSION);
        if (!configurationVersion.isNull()) log.info("Configuration: {}", configurationVersion.getValue());
        log.info("Configuration Source: {}", dataConversionConfigFilename);

        log.debug("DConvers. Load DataConversionConfigFile.");
        if (LibraryMode.PRESET == switches.getLibraryMode()) {
            dataConversionConfigFile.loadProperties();
            dataConversionConfigFile.validate();
        } else if (LibraryMode.MANUAL != switches.getLibraryMode()) {
            dataConversionConfigFile = new DataConversionConfigFile(this, dataConversionConfigFilename);
        }

        if (!dataConversionConfigFile.isValid()) {
            if (dataConversionConfigFile.isChildValid()) {
                performInvalidConfigFile();
            } else {
                performInvalidConfigChild();
            }
            if (isLibEnd) {
                return;
            }
        }

        errorCode = dataConversionConfigFile.getErrorCode();
        warningCode = dataConversionConfigFile.getWarningCode();
        successCode = dataConversionConfigFile.getSuccessCode();
        log.debug("successCode({}), errorCode({}), warningCode({})", successCode, errorCode, warningCode);

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

        DataString targetOutputPath = (DataString) systemVariableMap.get(SystemVariable.TARGET_OUTPUT_PATH);
        DataString mappingOutputPath = (DataString) systemVariableMap.get(SystemVariable.MAPPING_OUTPUT_PATH);
        DataString sourceOutputPath = (DataString) systemVariableMap.get(SystemVariable.SOURCE_OUTPUT_PATH);
        targetOutputPath.setValue(dataConversionConfigFile.getOutputTargetPath());
        mappingOutputPath.setValue(dataConversionConfigFile.getOutputMappingPath());
        sourceOutputPath.setValue(dataConversionConfigFile.getOutputSourcePath());

        // define user variables here
        addUserVariables(dataConversionConfigFile.getVariableList());

        tableSummary = new SummaryTable(this);
        tableSummary.setOwner(this);
        tableSummary.setQuery(SystemQuery.TABLE_SUMMARY.name());

        outputSummary = new SummaryTable(this);
        outputSummary.setOwner(this);
        outputSummary.setQuery(SystemQuery.OUTPUT_SUMMARY.name());

        systemTableMap = new HashMap<>();
        systemTableMap.put(DynamicValueType.SYS.name() + ":" + SystemQuery.TABLE_SUMMARY.name(), tableSummary);
        systemTableMap.put(DynamicValueType.SYS.name() + ":" + SystemQuery.OUTPUT_SUMMARY.name(), outputSummary);

        log.debug("DConvers. Load Plugins for calculator.");
        boolean loadPluginsSuccess = true;
        List<Pair<String, String>> pluginsList = dataConversionConfigFile.getPluginsCalcList();
        if (pluginsList != null) {
            log.debug("Has {} plugins for calculator.", pluginsList.size());
            for (Pair<String, String> plugins : pluginsList) {
                try {
                    CalcTypes.addPlugins(plugins.getKey(), plugins.getValue());
                } catch (ClassNotFoundException e) {
                    error("Load calculator plugins({}) is failed, class({}) not found!", plugins.getKey(), e.getMessage());
                    loadPluginsSuccess = false;
                }
            }
        }
        if (!loadPluginsSuccess && exitOnError) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
        }

        log.debug("DConvers. Load Plugins for output.");
        loadPluginsSuccess = true;
        pluginsList = dataConversionConfigFile.getPluginsOutputList();
        if (pluginsList != null) {
            log.debug("Has {} plugins for output.", pluginsList.size());
            for (Pair<String, String> plugins : pluginsList) {
                try {
                    log.debug("addPlugins({}, {}).", plugins.getKey(), plugins.getValue());
                    OutputTypes.addPlugins(plugins.getKey(), plugins.getValue());
                } catch (ClassNotFoundException e) {
                    error("Load output plugins({}) is failed, class({}) not found!", plugins.getKey(), e.getMessage());
                    loadPluginsSuccess = false;
                }
            }
        }
        if (!loadPluginsSuccess && exitOnError) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
        }

        log.debug("DConvers. Load DataSources.");
        if (dataSourceMap == null) {
            dataSourceMap = new HashMap<>();
        }
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
                    if (isLibEnd) {
                        return;
                    }
                }
                dataSourceMap.put(dataSourceName.toUpperCase(), dataSource);
            }
        } else if (exitOnError) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
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

        dataSourceName = Property.LINES.key();
        dataSourceMap.put(dataSourceName.toUpperCase(), new LinesDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));

        log.debug("DConvers. Load Plugins for datasource.");
        loadPluginsSuccess = true;
        pluginsList = dataConversionConfigFile.getPluginsDataSourceList();
        if (pluginsList != null) {
            log.debug("Has {} plugins for datasource.", pluginsList.size());
            for (Pair<String, String> plugins : pluginsList) {
                dataSourceName = plugins.getKey();
                DataSource pluginDataSource = InputFactory.getDataSource(this, dataSourceName, plugins.getValue());
                if (pluginDataSource == null) {
                    loadPluginsSuccess = false;
                    continue;
                }
                dataSourceMap.put(dataSourceName.toUpperCase(), pluginDataSource);
            }
        }
        if (!loadPluginsSuccess && exitOnError) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
        }

        log.debug("DConvers. Load SFTP Services.");
        sftpMap = new HashMap<>();
        SFTP sftp;
        String sftpName;
        HashMap<String, HostConfig> sftpConfigMap = dataConversionConfigFile.getSftpConfigMap();
        if (sftpConfigMap != null) {
            for (HostConfig sftpConfig : sftpConfigMap.values()) {
                sftpName = sftpConfig.getName();

                sftp = new SFTP(this, sftpName, sftpConfig);
                if (!sftp.isValid()) {
                    performInvalidSFTP(sftp);
                    if (isLibEnd) {
                        return;
                    }
                }
                sftpMap.put(sftpName.toUpperCase(), sftp);
            }
        } else if (exitOnError) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
        }

        log.debug("DConvers. Load SMTP Services.");
        smtpMap = new HashMap<>();
        SMTP smtp;
        String smtpName;
        HashMap<String, HostConfig> smtpConfigMap = dataConversionConfigFile.getSmtpConfigMap();
        if (smtpConfigMap != null) {
            for (HostConfig smtpConfig : smtpConfigMap.values()) {
                smtpName = smtpConfig.getName();

                smtp = new SMTP(this, smtpName, smtpConfig);
                if (!smtp.isValid()) {
                    performInvalidSMTP(smtp);
                    if (isLibEnd) {
                        return;
                    }
                }
                smtpMap.put(smtpName.toUpperCase(), smtp);
            }
        } else if (exitOnError) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
        }

        log.debug("DConvers. Load Converters.");
        converterList = new ArrayList<>();
        Converter converter;
        String converterName;
        HashMap<String, ConverterConfigFile> converterConfigMap = dataConversionConfigFile.getConverterConfigMap();
        if (converterConfigMap != null) {
            for (ConverterConfigFile converterConfigFile : converterConfigMap.values()) {
                converterConfigFile.loadConfig();
                converterName = converterConfigFile.getName();
                converter = new Converter(this, converterName, converterConfigFile);

                if (!converter.isValid()) {
                    if (exitOnError) {
                        performInvalidConverter(converter);
                        if (isLibEnd) {
                            return;
                        }
                    }
                    success = false;
                }

                converterList.add(converter);
            }
            log.info("Has {} converter(s)", converterList.size());
        } else if (exitOnError) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
        }

        Converter lastConverter = null;
        if (!switches.isTest()) {
            log.debug("DConvers. Launch Converters to transfer, transform and create output.");
            converterList.sort((o1, o2) -> o1.getConverterConfigFile().getIndex() > o2.getConverterConfigFile().getIndex() ? 1 : -1);

            if (converterList.size() > 0) {
                lastConverter = converterList.get(converterList.size() - 1);

                for (Converter convert : converterList) {
                    log.info("Converter({}) configuration file is '{}'", convert.getConverterConfigFile().getIndex(), convert.getName());
                    currentConverter = convert;

                    success = convert.printSources() && success;

                    success = convert.buildTargets() && success;
                    success = convert.printTarget() && success;

                    if (!switches.isLibrary() && !lastConverter.equals(convert)) {
                        convert.close();
                    }
                }
            }
        } else {
            log.info("IN TEST MODE: Skip all table operations.");
        }

        if (!success && exitOnError) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
        }

        currentConverter = null;
        if (lastConverter != null && !switches.isLibrary()) {
            lastConverter.close();
        }

        // Have some errors
        if (currentState.getLongValue() == errorCode) {
            stopWithError();
            if (isLibEnd) {
                return;
            }
        }

        // Successful with warning
        if (hasWarning) {
            stopWithWarning();
        }

        // Successful without warning
        stop();
    }

    /**
     * asLib: also used in normal mode too.
     */
    public void addUserVariables(List<Pair<String, String>> variableList) {
        if (variableList == null) {
            return;
        }

        DataColumn variable;
        String name;
        String value;
        int type;
        for (Pair<String, String> pair : variableList) {
            name = pair.getKey().toUpperCase();
            value = pair.getValue();
            type = getVariableType(value);

            variable = userVariableMap.get(name);
            if (variable == null) {
                variable = createDataColumn(0, name, type, value);
                userVariableMap.put(name, variable);
            } else {
                variable.setValue(value);
            }
        }
    }

    /**
     * asLib: addDataSource by Connection
     */
    public boolean addDataSource(String dataSourceName, Connection connection) {
        DataSource dataSource = new DataSource(this, dataSourceName, connection);
        if (dataSource.getDataSourceConfig().isValid()) {
            dataSourceMap.put(dataSourceName.toUpperCase(), dataSource);
            return true;
        }
        return false;
    }

    private int getVariableType(String value) {
        // 2.4566 or 24566
        if (NumberUtils.isCreatable(value)) {
            if (value.indexOf('.') >= 0) {
                return Types.DECIMAL;
            }
            return Types.INTEGER;
        }

        // '2018/07/30 21:12:38' or 'string'

        return Types.VARCHAR;
    }

    private void initSystemVariables() {
        userVariableMap = new HashMap<>();
        systemVariableMap = createSystemVariableMap();

        systemVariableMap.put(SystemVariable.NOW, new ComputeNow(this, "NOW"));
        systemVariableMap.get(SystemVariable.EMPTY_STRING).setValue("");
        ((DataDate) systemVariableMap.get(SystemVariable.APPLICATION_START)).setValue(appStartDate);

        createCurrentStateVar(Defaults.EXIT_CODE_SUCCESS.getLongValue());

        currentStateMessage = (DataString) systemVariableMap.get(SystemVariable.APPLICATION_STATE_MESSAGE);
        currentStateMessage.setValue("");

        VersionFormatter versionFormatter = new VersionFormatter(this);
        createVersionVar(versionFormatter);

        if (!switches.isLibrary()) {
            /*version of user configuration files is optional*/
            VersionConfigFile cVersionConfigFile = new VersionConfigFile(this, Property.VERSION_PROPERTIES.key());
            if (cVersionConfigFile.isValid()) {
                systemVariableMap.get(SystemVariable.CONFIG_VERSION).setValue(versionFormatter.versionString(cVersionConfigFile));
            }
        }
    }

    private void createCurrentStateVar(long exitCode) {
        currentState = (DataLong) systemVariableMap.get(SystemVariable.APPLICATION_STATE);
        currentState.setValue(exitCode);
    }

    private void createVersionVar(VersionFormatter versionFormatter) {
        VersionConfigFile versionConfigFile = new VersionConfigFile(this, Property.CURRENT_VERSION.key());
        systemVariableMap.get(SystemVariable.APPLICATION_VERSION).setValue(versionFormatter.versionNumber(versionConfigFile));
        systemVariableMap.get(SystemVariable.APPLICATION_FULL_VERSION).setValue(versionFormatter.versionString(versionConfigFile));
    }

    private void performTimeTracker() {
        timeTracker.stop(TimeTrackerKey.APPLICATION);
        log.debug(timeTracker.toString());
    }

    public void stop() {
        log.debug("DConvers.stop.");

        closeAllSFTP();
        closeAllDataSource();
        performTimeTracker();

        if (!switches.isHelp() && !switches.isVersion()) log.info("SUCCESS");

        int exitCode;
        if (dataConversionConfigFile == null) {
            exitCode = Defaults.EXIT_CODE_SUCCESS.getIntValue();
        } else {
            exitCode = dataConversionConfigFile.getSuccessCode();
        }
        log.debug("exitCode({})", exitCode);

        if (switches.isLibrary()) {
            isLibEnd = true;
        } else {
            System.exit(exitCode);
        }
    }

    public void stopWithError() {
        log.trace("DConvers.stopWithError.", new Exception());

        closeAllSFTP();
        closeAllDataSource();
        performTimeTracker();

        log.info("EXIT WITH SOME ERROR");

        int exitCode;
        if (dataConversionConfigFile == null) {
            exitCode = Defaults.EXIT_CODE_ERROR.getIntValue();
        } else {
            exitCode = (int) errorCode;
        }
        log.debug("exitCode({})", exitCode);

        if (switches.isLibrary()) {
            isLibEnd = true;
        } else {
            System.exit(exitCode);
        }
    }

    public void stopWithWarning() {
        log.debug("DConvers.stopWithWarning.");

        closeAllSFTP();
        closeAllDataSource();
        performTimeTracker();

        log.info("SUCCESSFUL WITH WARNING");


        int exitCode;
        if (dataConversionConfigFile == null) {
            exitCode = Defaults.EXIT_CODE_WARNING.getIntValue();
        } else {
            exitCode = (int) warningCode;
        }
        log.debug("exitCode({})", exitCode);

        if (switches.isLibrary()) {
            isLibEnd = true;
        } else {
            System.exit(exitCode);
        }
    }

    private void closeAllDataSource() {
        log.debug("DConvers.closeAllDataSource.");

        if (dataSourceMap == null) {
            return;
        }

        for (DataSource dataSource : dataSourceMap.values()) {
            dataSource.close();
        }
    }

    private void closeAllSFTP() {
        log.debug("DConvers.closeAllSFTP.");

        if (sftpMap == null) {
            return;
        }

        for (SFTP sftp : sftpMap.values()) {
            sftp.close();
        }
    }

    private void performInvalidSwitches() {
        log.debug("DConvers.performInvalidSwitches.");

        systemVariableMap = createSystemVariableMap();
        createCurrentStateVar(Defaults.EXIT_CODE_ERROR.getLongValue());
        createVersionVar(new VersionFormatter(this));

        error("Invalid CLI Switches, please see help below");
        log.debug("invalid switches: {}", switches);

        performHelp();
        stopWithError();
    }

    private void performInvalidConfigFile() {
        log.debug("DConvers.performInvalidConfigFile.");

        error("Invalid Configuration File({}) please check the file or see readme.md", switches.getSource());
        log.debug("dataConversionConfigFile = {}", dataConversionConfigFile);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidConfigChild() {
        log.debug("DConvers.performInvalidConfigChild.");

        error("Invalid Child of Configuration File({}) please check the file or see readme.md", switches.getSource());
        log.debug("dataConversionConfigFile = {}", dataConversionConfigFile);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidDataSource(DataSource dataSource) {
        log.debug("DConvers.performInvalidDataSource.");

        error("Invalid Datasource ({}) please check {}.", dataSource.getName(), dataConversionConfigFile.getName());
        log.debug("datasource = {}", dataSource);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidSFTP(SFTP sftp) {
        log.debug("DConvers.performInvalidSFTP.");

        error("Invalid SFTP({}) please check {}.", sftp.getName(), dataConversionConfigFile.getName());
        log.debug("sftp = {}", sftp);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidSMTP(SMTP smtp) {
        log.debug("DConvers.performInvalidSMTP.");

        error("Invalid SMTP({}) please check {}.", smtp.getName(), dataConversionConfigFile.getName());
        log.debug("smtp = {}", smtp);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performInvalidConverter(Converter converter) {
        log.debug("DConvers.performInvalidConverter.");

        error("Invalid Converter ({}) please check the configuration files or see readme.md", converter.getName());
        log.debug("converter = {}", converter);
        if (exitOnError) {
            stopWithError();
        }
    }

    private void performHelp() {
        log.debug("DConvers.performHelp.");
        printVersion();

        String syntax = "dconvers [switches]\n" +
                "\nSwitches:\n";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(syntax, switches.getOptions());

        System.out.println();

    }

    private void performVersion() {
        log.debug("DConvers.performVersion.");
        printVersion();
        System.out.println();
    }

    private void printVersion() {
        PrintWriter pw = new PrintWriter(System.out);
        pw.write(systemVariableMap.get(SystemVariable.APPLICATION_FULL_VERSION).getValue());
        pw.println();
        pw.flush();
    }

    //======= Utilities =======

    public SFTP getSFTP(String sftpName) {
        if (sftpName == null) {
            return null;
        }

        return sftpMap.get(sftpName.toUpperCase());
    }

    public SMTP getSMTP(String smtpName) {
        if (smtpName == null) {
            return null;
        }

        return smtpMap.get(smtpName.toUpperCase());
    }

    private HashMap<SystemVariable, DataColumn> createSystemVariableMap() {
        List<SystemVariable> systemVariableList = Arrays.asList(SystemVariable.values());
        HashMap<SystemVariable, DataColumn> variables = new HashMap<>();
        DataColumn dataColumn;

        for (SystemVariable systemVariable : systemVariableList) {
            dataColumn = createDataColumn(0, systemVariable.name(), systemVariable.getDataType(), null);
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
     * @param index      column index start from 1
     * @param columnName initial name
     * @param columnType see java.sql.Types for detailed
     * @return new instance of DataColumn depend on columnType
     */
    public DataColumn createDataColumn(int index, String columnName, int columnType, String value) {
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
                dataColumn = new DataLong(this, index, type, columnName, longValue);
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
                dataColumn = new DataBigDecimal(this, index, Types.DECIMAL, columnName, (doubleValue == null) ? null : BigDecimal.valueOf(doubleValue));
                break;

            case Types.DATE:
            case Types.TIMESTAMP:
                dataColumn = new DataDate(this, index, Types.DATE, columnName, value);
                break;

            /*case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                dataColumn = new DataString(this, index, Types.VARCHAR, columnName, value);
                break;*/
            default:
                dataColumn = new DataString(this, index, Types.VARCHAR, columnName, value);
        }

        //log.debug("createDataColumn(columnName:{}, valueAsString:{}) = {}", columnName, value == null ? "null" : "\"" + value + "\"", dataColumn);
        return dataColumn;
    }

    private Long toLong(String value) {
        int dot = value.indexOf(".");
        if (dot > 0) {
            value = value.substring(0, dot);
        }

        Long longValue;
        try {

            longValue = Long.parseLong(value);
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

    private File getFileForRead(String pathname) {
        pathname = pathname.trim();
        File file = new File(pathname);
        if (file.exists()) {
            return file;
        }

        file = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + pathname);
        return file;
    }

    public Reader getReader(String fileName) throws FileNotFoundException {
        String readerDomain = Property.READER.key();
        if (fileName.startsWith(readerDomain)) {
            String readerKey = fileName.substring(readerDomain.length());
            Reader reader = readerMap.get(readerKey);
            if (reader == null) {
                String message = "Reader not found for " + fileName;
                log.debug("{}, readerMap = {}", message, readerMap);
                throw new FileNotFoundException(message);
            }
            return reader;

        } else {
            return new FileReader(getFileForRead(fileName));
        }
    }
}
