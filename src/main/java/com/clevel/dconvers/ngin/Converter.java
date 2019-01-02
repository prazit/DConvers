package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.calc.Calc;
import com.clevel.dconvers.ngin.calc.CalcFactory;
import com.clevel.dconvers.ngin.calc.CalcTypes;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataLong;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.dynvalue.DynamicValueType;
import com.clevel.dconvers.ngin.output.OutputFactory;
import com.clevel.dconvers.ngin.output.OutputTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

public class Converter extends AppBase {

    private ConverterConfigFile converterConfigFile;
    private Map<String, Source> sourceMap;
    private Map<String, Target> targetMap;
    private Map<String, DataTable> mappingTableMap;
    private List<Target> sortedTarget;
    private List<Source> sortedSource;

    private DataTable currentTable;
    private int currentRowIndex;
    private boolean exitOnError;

    public Converter(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);
        this.converterConfigFile = converterConfigFile;

        exitOnError = application.exitOnError;

        valid = prepare();
        if (valid) {
            valid = validate();
        }

        log.trace("Converter({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Converter.class);
    }

    private boolean prepare() {
        log.trace("Converter({}).prepare", name);

        Map<String, SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        Map<String, TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();

        boolean valid;
        String name;
        Source source;
        sourceMap = new HashMap<>();
        sortedSource = new ArrayList<>();
        if (sourceConfigMap != null) {
            for (SourceConfig sourceConfig : sourceConfigMap.values()) {
                name = sourceConfig.getName();
                source = new Source(application, name, this, sourceConfig);
                valid = source.isValid();
                if (!valid && exitOnError) {
                    return false;
                }
                sourceMap.put(name.toUpperCase(), source);
                sortedSource.add(source);
            }
            sortedSource.sort((o1, o2) -> o1.getSourceConfig().getIndex() > o2.getSourceConfig().getIndex() ? 1 : -1);
        }

        Target target;
        targetMap = new HashMap<>();
        sortedTarget = new ArrayList<>();
        if (targetConfigMap != null) {
            for (TargetConfig targetConfig : targetConfigMap.values()) {
                name = targetConfig.getName();
                target = new Target(application, name, this, targetConfig);
                valid = target.isValid();
                if (!valid && exitOnError) {
                    return false;
                }
                targetMap.put(name.toUpperCase(), target);
                sortedTarget.add(target);
            }
            sortedTarget.sort((o1, o2) -> o1.getTargetConfig().getIndex() > o2.getTargetConfig().getIndex() ? 1 : -1);
        }

        mappingTableMap = new HashMap<>();

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Converter({}).validate", name);

        if (sourceMap.size() == 0) {
            log.debug("No source config in converter({}).", name);
        }

        if (targetMap.size() == 0) {
            log.debug("No target config in converter({})", name);
        }

        return true;
    }

    public boolean convert() {
        log.trace("Converter({}).convert", name);
        TimeTracker timeTracker = application.timeTracker;
        boolean success = true;

        DataTable sourceDataTable;
        for (Source source : sortedSource) {
            timeTracker.start(TimeTrackerKey.SOURCE, "buildDataTable for source(" + source.getName() + ")");
            sourceDataTable = source.getDataTable();
            timeTracker.stop(TimeTrackerKey.SOURCE);
            if (sourceDataTable == null) {
                success = false;
                if (exitOnError) {
                    return false;
                }
                continue;
            }

            log.info("SRC:{} has {} row(s)", source.getName(), sourceDataTable.getRowCount());
        }
        log.info("Retrieved, {} source(s).", sortedSource.size());

        for (Target target : sortedTarget) {
            timeTracker.start(TimeTrackerKey.TARGET, "buildDataTable for target(" + target.getName() + ")");
            valid = target.buildDataTable();
            timeTracker.stop(TimeTrackerKey.TARGET);
            if (!valid) {
                success = false;
                if (exitOnError) {
                    return false;
                }
            }

            for (DataTable mappingTable : target.getMappingTableList()) {
                mappingTableMap.put(mappingTable.getName().toUpperCase(), mappingTable);
            }
        }
        log.info("{} target-table(s) are built.", sortedTarget.size());

        return success;
    }

    public boolean print() {
        log.trace("Converter({}).print", name);
        boolean success = true;

        Map<SystemVariable, DataColumn> systemVariableMap = application.systemVariableMap;
        DataLong sourceFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        DataLong targetFileNumber = (DataLong) systemVariableMap.get(SystemVariable.TARGET_FILE_NUMBER);
        DataLong mappingFileNumber = (DataLong) systemVariableMap.get(SystemVariable.MAPPING_FILE_NUMBER);

        OutputConfig outputConfig;
        DataTable dataTable;

        // -- Outputs for Source Table

        List<OutputTypes> outputTypeList;
        for (Source source : sortedSource) {
            sourceFileNumber.increaseValueBy(1);

            dataTable = source.getDataTable();
            if (dataTable == null) {
                dataTable = new DataTable(application, source.getName(), "id", Collections.EMPTY_LIST, source);
            }
            setCurrentTable(dataTable);

            outputConfig = source.getSourceConfig().getOutputConfig();
            outputTypeList = outputConfig.getOutputTypeList();
            for (OutputTypes outputType : outputTypeList) {
                if (outputTypeList.size() == 0) {
                    log.debug("no output config for source({})", dataTable.getName());
                    continue;
                }

                log.trace("printing Source({}) to Output({})", source.getName(), outputType.name());
                if (!OutputFactory.getOutput(application, outputType).print(outputConfig, dataTable)) {
                    success = false;
                    if (exitOnError) {
                        return false;
                    }
                }
            }

        }

        // -- Outputs for Target Table and Mapping Table

        for (Target target : sortedTarget) {
            targetFileNumber.increaseValueBy(1);
            mappingFileNumber.increaseValueBy(1);

            // -- Outputs for Target Table

            dataTable = target.getDataTable();
            if (dataTable == null) {
                dataTable = new DataTable(application, target.getName(), "id", Collections.EMPTY_LIST, target);
            }
            setCurrentTable(dataTable);

            outputConfig = target.getTargetConfig().getOutputConfig();
            outputTypeList = outputConfig.getOutputTypeList();
            for (OutputTypes outputType : outputTypeList) {
                if (outputTypeList.size() == 0) {
                    log.debug("no output config for target({})", dataTable.getName());
                    continue;
                }

                log.trace("printing Target({}) to Output({})", target.getName(), outputType.name());
                if (!OutputFactory.getOutput(application, outputType).print(outputConfig, dataTable)) {
                    success = false;
                    if (exitOnError) {
                        return false;
                    }
                }
            }

            // -- Outputs for Mapping Table

            outputConfig = target.getTargetConfig().getMappingOutputConfig();
            outputTypeList = outputConfig.getOutputTypeList();
            for (DataTable mappingTable : target.getMappingTableList()) {
                setCurrentTable(mappingTable);
                if (outputTypeList.size() == 0) {
                    log.debug("no output config for mappingTable({})", mappingTable.getName());
                    continue;
                }

                for (OutputTypes outputType : outputTypeList) {
                    log.trace("printing Mapping({}) to Output({})", target.getName(), outputType.name());
                    if (!OutputFactory.getOutput(application, outputType).print(outputConfig, mappingTable)) {
                        success = false;
                        if (exitOnError) {
                            return false;
                        }
                    }
                }
            }

        }

        return success;
    }

    public ConverterConfigFile getConverterConfigFile() {
        return converterConfigFile;
    }

    public Source getSource(String name) {
        if (name == null) {
            log.debug("getSource(null) then return null");
            return null;
        }

        return sourceMap.get(name.toUpperCase());
    }

    public Target getTarget(String name) {
        if (name == null) {
            return null;
        }

        return targetMap.get(name.toUpperCase());
    }

    /**
     * @param dataTableMapping example: "SRC:MyTableName"
     * @return Source DataTable which has the specified name, otherwise return null
     */
    public DataTable getDataTable(String dataTableMapping) {
        DataTable dataTable;
        String[] mappings = dataTableMapping.split("[:]");
        if (mappings.length == 1) {
            mappings = new String[]{Property.SRC.name(), dataTableMapping};
        }

        DynamicValueType tableType = DynamicValueType.parse(mappings[0]);
        switch (tableType) {
            case SRC:
                Source source = getSource(mappings[1]);
                if (source == null) {
                    return null;
                }
                return source.getDataTable();

            case TAR:
                Target target = getTarget(mappings[1]);
                if (target == null) {
                    return null;
                }
                return target.getDataTable();

            case MAP:
                dataTable = mappingTableMap.get(mappings[1].toUpperCase());
                break;

            default:
                dataTable = null;
        }

        return dataTable;
    }

    public String compileDynamicValues(String sourceString) {
        if (sourceString == null) {
            return null;
        }

        String returnValue = sourceString;
        String compileResult;

        try {
            for (; true; returnValue = compileResult) {
                compileResult = compileFirstDynamicValue(returnValue);
                if (compileResult == null) {
                    return returnValue;
                }
            }
        } catch (Exception ex) {
            error("Converter.compileDynamicValues({}) has unexpected exception, {}", sourceString, ex);
            return null;
        }

    }

    private String compileFirstDynamicValue(String sourceString) {
        int start = sourceString.indexOf("$[");
        if (start < 0) {
            return null;
        }

        int end = sourceString.indexOf("]", start);
        if (end < 0) {
            end = sourceString.length() - 1;
        }

        String dynamicValue = sourceString.substring(start + 2, end);
        //log.debug("Converter.compileFirstDynamicValue: dynamicValue({})", dynamicValue);

        DataColumn dataColumn = getDynamicValue(dynamicValue);
        if (dataColumn == null) {
            return null;
        }

        String replacement;
        switch (dataColumn.getType()) {
            case Types.INTEGER:
            case Types.BIGINT:
                replacement = dataColumn.getFormattedValue(Defaults.NUMBER_FORMAT.getStringValue().replaceAll("[,]", ""));
                break;

            case Types.DECIMAL:
                replacement = dataColumn.getFormattedValue(Defaults.DECIMAL_FORMAT.getStringValue().replaceAll("[,]", ""));
                break;

            case Types.DATE:
            case Types.TIMESTAMP:
                replacement = dataColumn.getFormattedValue(Defaults.DATE_FORMAT.getStringValue());
                break;

            default:
                replacement = dataColumn.getValue();
        }

        return sourceString.substring(0, start) + replacement + sourceString.substring(end + 1);
    }

    public String valuesFromDataTable(String dataTableMapping, String columnName) {
        log.debug("Source.valuesFromDataTable(dataTableMapping:{}, columnName:{})", dataTableMapping, columnName);

        DataTable dataTable = getDataTable(dataTableMapping);
        if (dataTable == null) {
            log.warn("Source.valuesFromDataTable. The specified dataTable({}) is not found.", dataTableMapping);
            return "";
        }

        if (dataTable == null || dataTable.getRowCount() == 0) {
            log.warn("Source.valuesFromDataTable. dataTable({}) is empty.", dataTableMapping);
            return "";
        }

        if (dataTable.getRow(0).getColumn(columnName) == null) {
            log.warn("Source.valuesFromDataTable. The specified column({}) is not found in dataTable({}).", columnName, dataTableMapping);
            return "";
        }

        List<String> valueList = new ArrayList<>();
        for (DataRow row : dataTable.getRowList()) {
            valueList.add(row.getColumn(columnName).getQuotedValue());
        }
        valueList = valueList.stream()
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(valueList);
        String values = String.join(",", valueList);

        /*String values = "";
        for (String value : valueList) {
            values += value + ",";
        }
        values = values.substring(0, values.length() - 1);*/

        log.debug("Source.valuesFromDataTable. return-value={}", values);
        return values;
    }

    public String valueFromFile(String fileName) {

        BufferedReader br = null;
        String content = "";
        try {
            br = new BufferedReader(new FileReader(fileName));
            for (String line; (line = br.readLine()) != null; ) {
                content += line + "\n";
            }

        } catch (FileNotFoundException fx) {
            error("Converter.valueFromFile({}). file not found: {}", fileName, fx.getMessage());

        } catch (Exception ex) {
            error("Converter.valueFromFile({}). has exception: {}", fileName, ex);

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("close file {} is failed, {}", fileName, e);
                }
            }
        }

        return content;
    }

    public DataColumn getDynamicValue(String dynamicValue) {
        // dynamicValue look like this
        // $[VAR:SOURCE_FILE_NUMBER]
        // $[TXT:FILE_NAME.txt]
        // $[CAL:function(argument1,argument2)]
        //log.trace("Converter.getDynamicValue.");

        if (dynamicValue.length() < 5) {
            error("Invalid syntax for DynamicValue({})", dynamicValue);
            return null;
        }

        String valueType = dynamicValue.substring(0, 3);
        String valueIdentifier = dynamicValue.substring(4);
        //log.debug("valueType({}) valueIdentifier({})", valueType, valueIdentifier);

        DynamicValueType dynamicValueType = DynamicValueType.parse(valueType);
        if (dynamicValueType == null) {
            error("Invalid type({}) for DynamicValue({}), see 'Dynamic Value Types' for detailed", valueType, dynamicValue);
            return null;
        }

        DataColumn dataColumn = null;
        String value;
        switch (dynamicValueType) {
            case TXT:
                value = valueFromFile(valueIdentifier);
                if (value == null) {
                    return null;
                }
                dataColumn = application.createDataColumn(valueType, Types.VARCHAR, value);
                break;

            case CAL:
                String[] values = valueIdentifier.split("[()]");
                CalcTypes calcType = CalcTypes.parse(values[0]);
                if (calcType == null) {
                    error("Invalid Calculator({}) for DynamicValue({}), see 'Calculator Types' for detailed", values[0], dynamicValue);
                    return null;
                }
                Calc calculator = CalcFactory.getCalc(application, calcType);
                calculator.setArguments(values[1]);
                dataColumn = calculator.calc();
                if (dataColumn == null) {
                    return null;
                }
                break;

            case SRC:
            case TAR:
            case MAP:
                String[] dataTableParameters = dynamicValue.split("[.]");
                value = valuesFromDataTable(dataTableParameters[0], dataTableParameters[1]);
                if (value == null) {
                    return null;
                }
                dataColumn = application.createDataColumn(valueType, Types.VARCHAR, value);
                break;

            case VAR:
                SystemVariable systemVariable = SystemVariable.parse(valueIdentifier);
                if (systemVariable == null) {
                    error("Invalid SystemVariable({}) for DynamicValue({}), see 'System Variables' for detailed", valueIdentifier, dynamicValue);
                    return null;
                }
                dataColumn = application.systemVariableMap.get(systemVariable);
                break;

            case ARG:
                int argIndex = Integer.parseInt(valueIdentifier) - 1;
                String[] args = application.args;
                if (argIndex < 0) {
                    log.warn("Invalid argument index({}), argument index is start at 1", ++argIndex);
                    argIndex = 0;
                } else if (argIndex > args.length) {
                    log.warn("Invalid argument index({}), last argument index is {}", argIndex, args.length);
                    argIndex = args.length - 1;
                }
                dataColumn = application.createDataColumn("argument(" + (argIndex + 1) + ")", Types.VARCHAR, args[argIndex]);
                break;

            default:
                error("Invalid type({}) for DynamicValue({}), see 'Dynamic Value Types' for detailed", valueType, dynamicValue);
        }

        log.debug("Converter.getDynamicValue. dynamicValue({}) = dataColumn({})", dynamicValue, dataColumn);
        return dataColumn;
    }

    public DataTable getCurrentTable() {
        return currentTable;
    }

    public int getCurrentRowIndex() {
        return currentRowIndex;
    }

    public void setCurrentTable(DataTable currentTable) {
        this.currentTable = currentTable;
        if (currentTable == null) {
            log.debug("Converter({}).currentTable is null", name);
        } else {
            log.debug("Converter({}).currentTable is {}", name, currentTable.getName());
        }
    }

    public void setCurrentRowIndex(int currentRowIndex) {
        this.currentRowIndex = currentRowIndex;
    }

    public List<Source> getSourceList() {
        return sortedSource;
    }

    public void close() {
        valid = false;
        log = null;
        application = null;
        converterConfigFile = null;
        sourceMap = null;
        targetMap = null;
        mappingTableMap = null;
        sortedSource = null;
        sortedTarget = null;
        currentTable = null;
        currentRowIndex = 0;
        System.gc();
    }

}