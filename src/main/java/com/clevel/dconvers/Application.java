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
import javafx.util.Pair;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
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
    public HashMap<String, DataColumn> userVariableMap;

    public List<Converter> converterList;
    public Converter currentConverter;
    public DataLong currentState;

    public HashMap<String, DataTable> systemTableMap;
    public SummaryTable tableSummary;
    public SummaryTable outputSummary;

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
        this.args = Arrays.copyOf(args, args.length);
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

        if (switches.isVersion()) {
            performVersion();
            stop();
        }

        String dataConversionConfigFilename = switches.getSource();
        log.debug("Working directory is '{}'", System.getProperty("user.dir"));

        VersionFormatter versionFormatter = new VersionFormatter(this);
        log.info(versionFormatter.versionString(new VersionConfigFile(this))+" configuration file is '{}'.", dataConversionConfigFilename);

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

        DataString targetOutputPath = (DataString) systemVariableMap.get(SystemVariable.TARGET_OUTPUT_PATH);
        DataString mappingOutputPath = (DataString) systemVariableMap.get(SystemVariable.MAPPING_OUTPUT_PATH);
        DataString sourceOutputPath = (DataString) systemVariableMap.get(SystemVariable.SOURCE_OUTPUT_PATH);
        targetOutputPath.setValue(dataConversionConfigFile.getOutputTargetPath());
        mappingOutputPath.setValue(dataConversionConfigFile.getOutputMappingPath());
        sourceOutputPath.setValue(dataConversionConfigFile.getOutputSourcePath());

        // define user variables here
        setUserVariables(dataConversionConfigFile.getVariableList());

        tableSummary = new SummaryTable(this);
        tableSummary.setOwner(this);
        tableSummary.setQuery(SystemQuery.TABLE_SUMMARY.name());

        outputSummary = new SummaryTable(this);
        outputSummary.setOwner(this);
        outputSummary.setQuery(SystemQuery.OUTPUT_SUMMARY.name());

        systemTableMap = new HashMap<>();
        systemTableMap.put(DynamicValueType.SYS.name() + ":" + SystemQuery.TABLE_SUMMARY.name(), tableSummary);
        systemTableMap.put(DynamicValueType.SYS.name() + ":" + SystemQuery.OUTPUT_SUMMARY.name(), outputSummary);

        log.trace("Application. Load Plugins for calculator.");
        boolean loadPluginsSuccess = true;
        List<Pair<String, String>> pluginsList = dataConversionConfigFile.getPluginsCalcList();
        if (pluginsList != null) {
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
        }

        log.trace("Application. Load Plugins for output.");
        loadPluginsSuccess = true;
        pluginsList = dataConversionConfigFile.getPluginsOutputList();
        if (pluginsList != null) {
            for (Pair<String, String> plugins : pluginsList) {
                try {
                    OutputTypes.addPlugins(plugins.getKey(), plugins.getValue());
                } catch (ClassNotFoundException e) {
                    error("Load output plugins({}) is failed, class({}) not found!", plugins.getKey(), e.getMessage());
                    loadPluginsSuccess = false;
                }
            }
        }
        if (!loadPluginsSuccess && exitOnError) {
            stopWithError();
        }

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

        dataSourceName = Property.LINES.key();
        dataSourceMap.put(dataSourceName.toUpperCase(), new LinesDataSource(this, dataSourceName, new DataSourceConfig(this, dataSourceName)));


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

    public void setUserVariables(List<Pair<String, String>> variableList) {
        String name;
        String value;
        int type;

        DataColumn variable;
        if (variableList == null) {
            return;
        }

        for (Pair<String, String> pair : variableList) {
            name = pair.getKey().toUpperCase();
            value = pair.getValue();
            type = getVariableType(value);

            variable = userVariableMap.get(name);
            if (variable == null) {
                variable = createDataColumn(name, type, value);
                userVariableMap.put(name, variable);
            } else {
                variable.setValue(value);
            }
        }
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

        currentState = (DataLong) systemVariableMap.get(SystemVariable.APPLICATION_STATE);
        currentState.setValue((long) Defaults.EXIT_CODE_SUCCESS.getIntValue());

        VersionFormatter versionFormatter = new VersionFormatter(this);
        VersionConfigFile versionConfigFile = new VersionConfigFile(this);
        systemVariableMap.get(SystemVariable.APPLICATION_VERSION).setValue(versionFormatter.versionNumber(versionConfigFile));
        systemVariableMap.get(SystemVariable.APPLICATION_FULL_VERSION).setValue(versionFormatter.versionString(versionConfigFile));
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

        int exitCode;
        if (dataConversionConfigFile == null) {
            exitCode = Defaults.EXIT_CODE_SUCCESS.getIntValue();
        } else {
            exitCode = dataConversionConfigFile.getSuccessCode();
        }
        System.exit(exitCode);
    }

    public void stopWithError() {
        log.trace("Application.stopWithError.");

        closeAllSFTP();
        closeAllDataSource();
        performTimeTracker();

        log.info("EXIT WITH SOME ERROR");

        int exitCode;
        if (dataConversionConfigFile == null) {
            exitCode = Defaults.EXIT_CODE_ERROR.getIntValue();
        } else {
            exitCode = dataConversionConfigFile.getErrorCode();
        }
        System.exit(exitCode);
    }

    public void stopWithWarning() {
        log.trace("Application.stopWithWarning.");

        closeAllSFTP();
        closeAllDataSource();
        performTimeTracker();

        log.info("SUCCESSFUL WITH WARNING");


        int exitCode;
        if (dataConversionConfigFile == null) {
            exitCode = Defaults.EXIT_CODE_WARNING.getIntValue();
        } else {
            exitCode = dataConversionConfigFile.getWarningCode();
        }
        System.exit(exitCode);
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
        printVersion();

        String syntax = "dconvers [switches]\n" +
                "\nSwitches:\n";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(syntax, switches.getOptions());

        System.out.println();
    }

    private void performVersion(){
        log.trace("Application.performVersion.");
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
                dataColumn = new DataBigDecimal(this, 0, Types.DECIMAL, columnName, (doubleValue == null) ? null : BigDecimal.valueOf(doubleValue));
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

    public File getFileForRead(String pathname) {
        pathname = pathname.trim();
        File file = new File(pathname);
        if (file.exists()) {
            return file;
        }

        file = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + pathname);
        return file;
    }

}
