package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.calc.Calc;
import com.clevel.dconvers.calc.CalcFactory;
import com.clevel.dconvers.calc.CalcTypes;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.data.*;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.dynvalue.Operator;
import com.clevel.dconvers.dynvalue.OperatorFactory;
import com.clevel.dconvers.dynvalue.OperatorType;
import com.clevel.dconvers.output.OutputFactory;
import com.clevel.dconvers.output.OutputTypes;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Converter extends AppBase {

    private ConverterConfigFile converterConfigFile;
    private HashMap<String, Source> sourceMap;
    private HashMap<String, Target> targetMap;
    private HashMap<String, DataTable> mappingTableMap;
    private List<Target> sortedTarget;
    private List<Source> sortedSource;

    private DataTable currentTable;
    private int currentRowIndex;
    private boolean exitOnError;

    public Converter(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);
        this.converterConfigFile = converterConfigFile;
        application.currentConverter = this;

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

        HashMap<String, SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        HashMap<String, TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();

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

    public boolean buildTargets() {
        log.trace("Converter({}).buildTargets", name);
        TimeTracker timeTracker = application.timeTracker;
        boolean success = true;

        DataTable targetDataTable;
        SummaryTable tableSummary = application.tableSummary;
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

            targetDataTable = target.getDataTable();
            tableSummary.addRow(name, targetDataTable.getName(), DynamicValueType.TAR, targetDataTable.getRowCount());

            for (DataTable mappingTable : target.getMappingTableList()) {
                mappingTableMap.put(mappingTable.getName().toUpperCase(), mappingTable);
                tableSummary.addRow(name, mappingTable.getName(), DynamicValueType.MAP, mappingTable.getRowCount());
            }
        }
        log.info("{} target-table(s) are built.", sortedTarget.size());

        return success;
    }

    public boolean printSources() {
        log.trace("Converter({}).printSources", name);
        boolean success = true;

        HashMap<SystemVariable, DataColumn> systemVariableMap = application.systemVariableMap;
        DataLong sourceFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        TimeTracker timeTracker = application.timeTracker;

        List<OutputTypes> outputTypeList;
        OutputConfig outputConfig;
        DataTable dataTable;
        String outputName;

        SummaryTable tableSummary = application.tableSummary;
        SummaryTable outputSummary = application.outputSummary;

        for (Source source : sortedSource) {
            sourceFileNumber.increaseValueBy(1);

            timeTracker.start(TimeTrackerKey.SOURCE, "build table for source(" + source.getName() + ")");
            log.info("Loading data for source({})", source.getName());
            dataTable = source.getDataTable();
            timeTracker.stop(TimeTrackerKey.SOURCE);

            if (dataTable == null) {
                success = false;
                if (exitOnError) {
                    return false;
                }
                dataTable = new DataTable(application, source.getName(), "source_id", source);
            }

            tableSummary.addRow(name, dataTable.getName(), DynamicValueType.SRC, dataTable.getRowCount());

            //------- print source -------

            setCurrentTable(dataTable);

            outputConfig = source.getSourceConfig().getOutputConfig();
            outputTypeList = outputConfig.getOutputTypeList();
            if (outputTypeList.size() == 0) {
                log.debug("no output config for source({})", dataTable.getName());
                source.printed();
                continue;
            }

            for (OutputTypes outputType : outputTypeList) {
                log.trace("printing Source({}) to Output({})", source.getName(), outputType.name());
                outputName = OutputFactory.getOutput(application, outputType).print(outputConfig, dataTable);
                if (outputName == null) {
                    success = false;
                    if (exitOnError) {
                        return false;
                    }
                }
                outputSummary.addRow(name, DynamicValueType.SRC, dataTable.getName(), outputType, outputName, dataTable.getRowCount());
            }
            source.printed();
        }

        return success;
    }

    public boolean printTarget() {
        log.trace("Converter({}).printTarget", name);
        boolean success = true;

        HashMap<SystemVariable, DataColumn> systemVariableMap = application.systemVariableMap;
        DataLong targetFileNumber = (DataLong) systemVariableMap.get(SystemVariable.TARGET_FILE_NUMBER);
        DataLong mappingFileNumber = (DataLong) systemVariableMap.get(SystemVariable.MAPPING_FILE_NUMBER);

        List<OutputTypes> outputTypeList;
        OutputConfig outputConfig;
        DataTable dataTable;
        String outputName;

        SummaryTable outputSummary = application.outputSummary;

        for (Target target : sortedTarget) {
            targetFileNumber.increaseValueBy(1);
            mappingFileNumber.increaseValueBy(1);

            // -- Outputs for Target Table Before Transformed

            outputConfig = target.getTargetConfig().getTransferOutputConfig();
            if (outputConfig.needOutput()) {
                dataTable = target.getDataTableBeforeTransform();
                if (dataTable != null) {
                    setCurrentTable(dataTable);
                    log.debug("Print data before transformed {} row(s)", dataTable.getRowCount());

                    if (!dataTable.print(outputConfig)) {
                        error("Print data before transformed is failed!");
                        success = false;
                        if (exitOnError) {
                            return false;
                        }
                    }
                }
            }

            // -- Outputs for Target Table

            dataTable = target.getDataTable();
            if (dataTable == null) {
                dataTable = new DataTable(application, target.getName(), "target_id", target);
            }
            setCurrentTable(dataTable);

            outputConfig = target.getTargetConfig().getOutputConfig();
            if (outputConfig.needOutput()) {

                outputTypeList = outputConfig.getOutputTypeList();
                if (outputTypeList.size() == 0) {
                    log.debug("no output config for target({})", dataTable.getName());
                    continue;
                }
                for (OutputTypes outputType : outputTypeList) {
                    log.trace("printing Target({}) to Output({})", target.getName(), outputType.name());
                    outputName = OutputFactory.getOutput(application, outputType).print(outputConfig, dataTable);
                    if (outputName == null) {
                        success = false;
                        if (exitOnError) {
                            return false;
                        }
                        log.debug("no output config for target({})", dataTable.getName());
                        continue;
                    }
                    outputSummary.addRow(name, DynamicValueType.TAR, dataTable.getName(), outputType, outputName, dataTable.getRowCount());
                }
            }

            // -- Outputs for Mapping Table

            outputConfig = target.getTargetConfig().getMappingOutputConfig();
            if (outputConfig.needOutput()) {
                outputTypeList = outputConfig.getOutputTypeList();
                for (DataTable mappingTable : target.getMappingTableList()) {
                    setCurrentTable(mappingTable);
                    if (outputTypeList.size() == 0) {
                        log.debug("no output config for mappingTable({})", mappingTable.getName());
                        continue;
                    }

                    for (OutputTypes outputType : outputTypeList) {
                        log.trace("printing Mapping({}) to Output({})", target.getName(), outputType.name());
                        outputName = OutputFactory.getOutput(application, outputType).print(outputConfig, mappingTable);
                        if (outputName == null) {
                            success = false;
                            if (exitOnError) {
                                return false;
                            }
                            continue;
                        }
                        outputSummary.addRow(name, DynamicValueType.MAP, dataTable.getName(), outputType, outputName, dataTable.getRowCount());
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
            log.debug("getTarget(null) then return null");
            return null;
        }

        return targetMap.get(name.toUpperCase());
    }

    public DataTable getMapping(String name) {
        if (name == null) {
            log.debug("getMapping(null) then return null");
            return null;
        }

        return mappingTableMap.get(name.toUpperCase());
    }

    /**
     * @param tableIdentifier example: "SRC:MyTableName"
     * @return Source DataTable which has the specified name, otherwise return null
     */
    public DataTable getDataTable(String tableIdentifier) {
        if (tableIdentifier == null) {
            return null;
        }

        DataTable dataTable;

        if (Property.CURRENT.key().equalsIgnoreCase(tableIdentifier)) {
            dataTable = currentTable;
            if (dataTable == null) {
                log.debug("getDataTable({}). current table is null! in converter({})", tableIdentifier, name);
                return null;
            }
            return dataTable;
        }

        String[] mappings = tableIdentifier.split("[:]");
        if (mappings.length == 1) {
            mappings = new String[]{Property.SRC.name(), tableIdentifier};
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
                dataTable = getMapping(mappings[1]);
                break;

            case SYS:
                dataTable = getSystemTable(mappings[1]);
                break;

            default:
                dataTable = null;
        }

        return dataTable;
    }

    private DataTable getSystemTable(String name) {
        if (name == null) {
            log.debug("getSystemTable(null) then return null");
            return null;
        }

        return application.systemTableMap.get(name.toUpperCase());
    }

    public DataRow getDataRow(String rowIdentifier, DataTable dataTable) {
        if (dataTable == null || rowIdentifier == null) {
            return null;
        }

        if (Property.CURRENT.key().equalsIgnoreCase(rowIdentifier)) {
            DataRow row = dataTable.getRow(currentRowIndex);
            if (row.getColumnCount() == 0) {
                return null;
            }
            return row;
        }

        DataRow row = dataTable.getRow(Integer.parseInt(rowIdentifier) - 1);
        if (row.getColumnCount() == 0) {
            return null;
        }

        return row;
    }

    public DataColumn getDataColumn(String columnIdentifier, DataRow dataRow) {
        if (dataRow == null || columnIdentifier == null) {
            return null;
        }

        DataColumn dataColumn;
        if (NumberUtils.isCreatable(columnIdentifier)) {
            dataColumn = dataRow.getColumn(NumberUtils.createInteger(columnIdentifier) - 1);
        } else {
            dataColumn = dataRow.getColumn(columnIdentifier);
        }

        return dataColumn;
    }

    private String[] parseExpression(char[] operators, String expression) {
        List<String> tokenList = new ArrayList<>();
        char[] chars = expression.toCharArray();
        int length = chars.length;
        int tokenStart = 0;
        char aChar;

        for (int index = 0; index < length; index++) {
            aChar = chars[index];
            if (ArrayUtils.indexOf(operators, aChar) < 0) {
                continue;
            }

            tokenList.add(expression.substring(tokenStart, index));
            tokenList.add(String.valueOf(aChar));
            tokenStart = index + 1;
        }
        tokenList.add(expression.substring(tokenStart));

        String[] tokens = new String[tokenList.size()];
        tokens = tokenList.toArray(tokens);
        return tokens;
    }

    private String computeExpression(String[] tokens) {
        int tokenLastIndex = tokens.length - 1;
        if (tokenLastIndex < 0) {
            return null;
        }
        if (tokenLastIndex == 0) {
            return tokens[0];
        }

        int tokenIndex = 0;
        String lastOperand = tokens[0];
        String leftOperand;
        String operator;
        String rightOperand;

        while (tokenIndex < tokenLastIndex) {
            if (tokenIndex + 2 > tokenLastIndex) {
                error("Invalid expression({})", (Object[]) tokens);
                return null;
            }

            leftOperand = lastOperand;
            operator = tokens[tokenIndex + 1];
            rightOperand = tokens[tokenIndex + 2];

            OperatorType operatorType = OperatorType.parse(operator.charAt(0));
            if (operatorType == null) {
                error("Invalid operator({}) in expression({})", operator, tokens);
                return null;
            }

            Operator computer = OperatorFactory.getOperator(application, operatorType);
            if (computer == null) {
                return null;
            }

            lastOperand = computer.compute(leftOperand, rightOperand);
            tokenIndex += 2;
        }

        return lastOperand;

    }

    public List<String> compileDynamicValues(List<String> sourceStringList) {
        Converter currentConverter = application.currentConverter;
        List<String> values = new ArrayList<>();

        for (String sql : sourceStringList) {
            values.add(currentConverter.compileDynamicValues(sql));
        }

        return values;
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

        String dynamicValueExpression = sourceString.substring(start + 2, end);
        String[] dynamicValues = parseExpression(OperatorType.getOperators(), dynamicValueExpression);
        if (dynamicValues == null) {
            log.debug("Converter.compileFirstDynamicValue.parseExpression: dynamicValueExpression({}) return null", dynamicValueExpression);
            return null;
        }
        log.debug("Converter.compileFirstDynamicValue: dynamicValueExpression({}) return dynamicValues({})", dynamicValueExpression, ArrayUtils.toString(dynamicValues));

        // turn dynamicValue-array to token-array for computeExpression method.
        String dynamicValue;
        for (int index = 0; index < dynamicValues.length; index++) {
            dynamicValue = dynamicValues[index];
            if (dynamicValue.length() == 1) {
                continue;
            }

            dynamicValue = getDynamicString(dynamicValue);
            if (dynamicValue == null) {
                return null;
            }
            dynamicValues[index] = dynamicValue;
        }

        String replacement = computeExpression(dynamicValues);
        return sourceString.substring(0, start) + replacement + sourceString.substring(end + 1);
    }

    public String getDynamicString(String dynamicValue) {
        DataColumn dataColumn = getDynamicValue(dynamicValue);
        if (dataColumn == null) {
            return null;
        }

        String string;
        switch (dataColumn.getType()) {
            case Types.INTEGER:
            case Types.BIGINT:
                string = dataColumn.getFormattedValue(Defaults.NUMBER_FORMAT.getStringValue().replaceAll("[,]", ""));
                break;

            case Types.DECIMAL:
                string = dataColumn.getFormattedValue(Defaults.DECIMAL_FORMAT.getStringValue().replaceAll("[,]", ""));
                break;

            case Types.DATE:
            case Types.TIMESTAMP:
                string = dataColumn.getFormattedValue(Defaults.DATE_FORMAT.getStringValue());
                break;

            default:
                string = dataColumn.getValue();
        }

        return string;
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
                String[] csvParameters = dynamicValue.split("[,]");
                String[] dataTableParameters = csvParameters[0].split("[.]");
                value = valuesFromDataTable(dataTableParameters[0], dataTableParameters[1]);
                if (value == null || value.isEmpty()) {
                    if (csvParameters.length == 1) {
                        return null;
                    }

                    /*use specified value when got empty csv*/
                    value = csvParameters[1];
                }
                dataColumn = application.createDataColumn(valueType, Types.VARCHAR, value);
                break;

            case VAR:
                SystemVariable systemVariable = SystemVariable.parse(valueIdentifier);
                if (systemVariable != null) {
                    log.debug("dynamicValue({}) is System Variable({})", valueIdentifier, systemVariable.name());
                    dataColumn = application.systemVariableMap.get(systemVariable);
                } else {
                    if (valueIdentifier == null) {
                        log.debug("dynamicValue({}) is null!", valueIdentifier);
                        dataColumn = null;
                    } else {
                        String userVariableName = valueIdentifier.toUpperCase();
                        log.debug("dynamicValue({}) is User Variable({})", valueIdentifier, userVariableName);
                        dataColumn = application.userVariableMap.get(userVariableName);
                    }
                    if (dataColumn == null) {
                        log.debug("unknown variable({})", valueIdentifier);
                        dataColumn = application.createDataColumn(valueIdentifier, Types.VARCHAR, "NULL");
                    }
                }
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

            case STR: // constant for STR, INT, DTE, DTT, DEC
            case INT:
            case DEC:
            case DTE:
            case DTT:
                if (valueIdentifier.compareToIgnoreCase("NULL") == 0) {
                    valueIdentifier = null;
                } else if (valueIdentifier.contains("$[")) {
                    valueIdentifier = compileDynamicValues(valueIdentifier);
                }

                dataColumn = application.createDataColumn(valueType, dynamicValueType.getDataType(), valueIdentifier);
                if (dataColumn == null) {
                    error("Invalid constant({}) for {} ", valueIdentifier, valueType);
                    return null;
                }

                if (dataColumn.getType() == Types.VARCHAR) {
                    value = dataColumn.getValue();
                    value = compileDynamicValues(value);
                    dataColumn.setValue(value);
                }
                break;

            default:
                error("Invalid type({}) for DynamicValue({}), see 'Dynamic Value Types' for detailed", valueType, dynamicValue);
        }

        //log.debug("Converter.getDynamicValue. dynamicValue({}) = dataColumn({})", dynamicValue, dataColumn);
        return dataColumn;
    }

    public String valuesFromDataTable(String dataTableMapping, String columnName) {
        log.debug("Source.valuesFromDataTable(dataTableMapping:{}, columnName:{})", dataTableMapping, columnName);

        DataTable dataTable = getDataTable(dataTableMapping);
        if (dataTable == null) {
            log.warn("Source.valuesFromDataTable. The specified dataTable({}) is not found.", dataTableMapping);
            return "";
        }

        if (dataTable.getRowCount() == 0) {
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
        StringBuilder content = new StringBuilder();
        String newLine = "\n";
        String value = null;
        int newLineLength = newLine.length();
        try {
            br = new BufferedReader(new FileReader(fileName));
            for (String line; (line = br.readLine()) != null; ) {
                content.append(line).append("\n");
            }
            value = content.toString().substring(0, content.length() - newLineLength);

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

        return value;
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
        /*System.gc()*/// FindBugs says this is dubious but if you get OutOfMemoryException you may be need to uncomment this again.
    }

}