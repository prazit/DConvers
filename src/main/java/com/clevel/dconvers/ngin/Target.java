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
import com.clevel.dconvers.ngin.transform.Transform;
import com.clevel.dconvers.ngin.transform.TransformFactory;
import com.clevel.dconvers.ngin.transform.TransformTypes;
import javafx.util.Pair;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Target extends AppBase {

    private Converter converter;
    private TargetConfig targetConfig;

    private DataTable dataTable;

    private List<String> sourceList;
    private List<DataTable> mappingTableList;

    public Target(Application application, String name, Converter converter, TargetConfig targetConfig) {
        super(application, name);
        this.converter = converter;
        this.targetConfig = targetConfig;

        valid = prepare();
        if (valid) valid = validate();

        log.trace("Target({}) is created", name);
    }

    private boolean prepare() {
        log.trace("Target({}).prepare.", name);

        sourceList = targetConfig.getSourceList();
        mappingTableList = new ArrayList<>();

        return true;
    }

    @Override
    public boolean validate() {
        return true;
    }

    private String getMappingTableName(String sourceTableName, String targetTableName) {
        if (sourceTableName.indexOf(":") > 0) {
            sourceTableName = sourceTableName.replaceAll("[:]", "_");
        } else {
            sourceTableName = "SRC_" + sourceTableName;
        }
        String name = sourceTableName + "_TO_" + targetTableName;
        return name.toUpperCase();
    }

    public boolean buildDataTable() {
        log.trace("Target({}).buildDataTable.", name);

        List<Pair<String, String>> columnList = targetConfig.getColumnList();

        String targetIdColumnName = targetConfig.getId();

        String mappingTableName;
        String mappingSourceIdColumnName = Property.SOURCE_ID.key();
        String mappingTargetIdColumnName = Property.TARGET_ID.key();

        dataTable = new DataTable(application, name, targetIdColumnName, targetConfig.getOutputConfig().getSqlPostSQL(), this);
        converter.setCurrentTable(dataTable);

        Map<SystemVariable, DataColumn> systemVars = application.systemVariableMap;
        DataLong varRowNumber = (DataLong) systemVars.get(SystemVariable.ROW_NUMBER);
        varRowNumber.setValue(targetConfig.getRowNumberStartAt() - 1);

        DynamicValueType sourceColumnType;
        String sourceColumnTypeArg;
        DataColumn targetColumn;
        String sourceColumnName;
        String targetColumnName;
        DataRow targetRow;
        DataRow mappingRow;
        int targetColumnIndex;

        Source source;
        DataTable sourceDataTable;
        String sourceIdColumnName;
        List<DataRow> sourceRowList;
        int rowCount;

        DataTable mappingTable;
        ProgressBar progressBar;
        for (String sourceName : sourceList) {
            log.debug("Target({}).buildDataTable from sourceName({})", name, sourceName);

            mappingTableName = getMappingTableName(sourceName, name);
            mappingTable = new DataTable(application, mappingTableName, mappingTargetIdColumnName);
            mappingTableList.add(mappingTable);

            if (sourceName.indexOf(":") > 0) {
                sourceDataTable = converter.getDataTable(sourceName);
            } else {
                source = converter.getSource(sourceName);
                if (source == null) {
                    error("Source({}) is not found, required by Target({})", sourceName, name);
                    valid = false;
                    return false;
                }
                sourceDataTable = source.getDataTable();
            }

            if (sourceDataTable == null) {
                error("Source({}) is not found, required by Target({})", sourceName, name);
                valid = false;
                return false;
            }
            mappingTable.setOwner(new Pair<>(sourceDataTable, dataTable));

            sourceIdColumnName = sourceDataTable.getIdColumnName();
            sourceRowList = sourceDataTable.getRowList();
            rowCount = sourceRowList.size();

            if (rowCount > Defaults.PROGRESS_SHOW_KILO_AFTER.getLongValue()) {
                progressBar = new ProgressBar("Build target(" + name + ")", rowCount, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, "K", 1000);
            } else {
                progressBar = new ProgressBar("Build target(" + name + ")", rowCount, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, " rows", 1);
            }
            progressBar.maxHint(rowCount);

            for (DataRow sourceRow : sourceRowList) {
                progressBar.step();

                // -- start target table

                varRowNumber.increaseValueBy(1);
                targetRow = new DataRow(application, dataTable);
                targetColumnIndex = 0;
                for (Pair<String, String> columnPair : columnList) {
                    targetColumnIndex++;
                    targetColumnName = columnPair.getKey();
                    sourceColumnName = columnPair.getValue();

                    sourceColumnType = parseDynamicValueType(sourceColumnName);
                    sourceColumnTypeArg = sourceColumnName.length() > 4 ? sourceColumnName.substring(4) : "";

                    targetColumn = getTargetColumn(progressBar, sourceName, sourceRow, sourceColumnName, sourceColumnType, sourceColumnTypeArg, targetColumnName, targetColumnIndex);
                    if (targetColumn == null) {
                        valid = false;
                        return false;
                    }

                    targetRow.putColumn(targetColumnName, targetColumn);
                } // end of for(ColumnPair)
                dataTable.addRow(targetRow);

                // -- start mapping table

                mappingRow = new DataRow(application, mappingTable);

                targetColumn = targetRow.getColumn(targetIdColumnName);
                if (targetColumn == null) {
                    progressBar.close();
                    error("Invalid target id({}) for target({}) that required by mapping table({})", targetIdColumnName, name, mappingTableName);
                    valid = false;
                    return false;
                }
                mappingRow.putColumn(mappingTargetIdColumnName, targetColumn.clone(1, mappingTargetIdColumnName));

                targetColumn = sourceRow.getColumn(sourceIdColumnName);
                if (targetColumn == null) {
                    progressBar.close();
                    error("Invalid source id({}) for source({}) that required by mapping table({})", sourceIdColumnName, sourceName, mappingTableName);
                    valid = false;
                    return false;
                }
                mappingRow.putColumn(mappingSourceIdColumnName, targetColumn.clone(1, mappingSourceIdColumnName));

                mappingTable.addRow(mappingRow);

            } // end of for(DataRow)

            progressBar.close();

            log.info("transferred from source({})", sourceName);
            log.info("TAR:{} has {} row(s)", name, dataTable.getRowCount());
            log.info("MAP:{} has {} row(s)", mappingTableName, dataTable.getRowCount());

        } // end of for sourceList

        // -- start report table

        // TODO: create reportRow and add into the application.reportTable

        // -- start transformation
        List<Pair<TransformTypes, Map<String, String>>> transformList = targetConfig.getTransformConfig().getTransformList();
        Map<String, String> argumentList;
        TransformTypes transformType;
        Transform transform;
        for (Pair<TransformTypes, Map<String, String>> transformPair : transformList) {
            transformType = transformPair.getKey();
            argumentList = transformPair.getValue();
            log.trace("transforming Target({}) by Transform({})", name, transformType.name());

            transform = TransformFactory.getTransform(application, transformType);
            transform.setArgumentList(argumentList);
            if (!transform.transform(dataTable)) {
                log.debug("Transform({}) is failed in Target({})", name, transformType.name());
                valid = false;
                return false;
            }
            log.info("TAR:{} is transformed by {}({}), remain {} row(s) {} column(s)", name, transformType.name(), argumentList, dataTable.getRowCount(), (dataTable.getRowCount() == 0) ? 0 : dataTable.getRow(0).getColumnCount());
        }

        return true;
    }

    private DynamicValueType parseDynamicValueType(String sourceColumnName) {
        if (sourceColumnName.length() < 5) {
            return DynamicValueType.NON;
        }

        if (sourceColumnName.indexOf(">>") >= 0) {
            return DynamicValueType.COL;
        }

        char ch = sourceColumnName.charAt(3);
        if (ch != ':') {
            return DynamicValueType.NON;
        }

        String keyWord = sourceColumnName.substring(0, 3).toUpperCase();
        DynamicValueType dynamicValueType;
        try {
            dynamicValueType = DynamicValueType.parse(keyWord);
        } catch (IllegalArgumentException e) {
            return DynamicValueType.INV;
        }

        return dynamicValueType;
    }

    private DataColumn getTargetColumn(ProgressBar progressBar, String sourceName, DataRow sourceRow, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnTypeArg, String targetColumnName, int targetColumnIndex) {
        DataColumn targetColumn;

        switch (sourceColumnType) {
            case COL:
                String[] sourceColumnNames = sourceColumnName.split(">>");
                sourceColumnName = sourceColumnNames[0];
                if (sourceColumnName.indexOf(":") > 0) {
                    DynamicValueType sourceColumnType2 = parseDynamicValueType(sourceColumnName);
                    String sourceColumnTypeArg2 = sourceColumnName.length() > 4 ? sourceColumnName.substring(4) : "";
                    targetColumn = getTargetColumn(progressBar, sourceName, sourceRow, sourceColumnName, sourceColumnType2, sourceColumnTypeArg2, targetColumnName, targetColumnIndex);
                } else {
                    targetColumn = sourceRow.getColumn(sourceColumnName);
                }

                if (targetColumn == null) {
                    progressBar.close();
                    error("No column({}) in source({}) that required by target({})", sourceColumnName, sourceName, name);
                    log.debug("source({}) has following columns {}", sourceName, sourceRow);
                    return null;
                }

                if (sourceColumnNames.length == 1) {
                    return targetColumn;
                }
                String[] mappings = sourceColumnNames[1].split("[.]");

                DataTable asSourceTable = converter.getDataTable(mappings[0]);
                if (asSourceTable == null) {
                    progressBar.close();
                    error("No table({}) in converter({}) that required by target({})", mappings[0], converter.getName(), name);
                    return null;
                }

                sourceColumnName = mappings[1];
                DataRow asSourceRow = asSourceTable.getRow(sourceColumnName, targetColumn.getValue());
                if (asSourceRow == null) {
                    progressBar.close();
                    error("No row contains column({}) with value({}) in a table({}) in converter({}) that required by target({})", sourceColumnName, targetColumn.getValue(), asSourceTable.getName(), converter.getName(), name);
                    log.debug("asSourceTable = {}", asSourceTable);
                    return null;
                }

                sourceColumnName = sourceColumnNames[2];
                targetColumn = asSourceRow.getColumn(sourceColumnName);
                if (targetColumn == null) {
                    progressBar.close();
                    error("No column({}) in data-table({}) in converter({}) that required by target({})", sourceColumnName, mappings[0], converter.getName(), name);
                    return null;
                }

                targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                break;

            case TXT:
                String value = converter.valueFromFile(sourceColumnName);
                if (value == null) {
                    return null;
                }
                targetColumn = application.createDataColumn(DynamicValueType.TXT.name(), Types.VARCHAR, value);
                break;

            case CAL:
                String[] values = sourceColumnTypeArg.split("[()]");
                CalcTypes calcType = CalcTypes.parse(values[0]);
                if (calcType == null) {
                    error("Invalid Calculator({}) for target column({})", sourceColumnTypeArg, targetColumnName);
                    return null;
                }
                Calc calculator = CalcFactory.getCalc(application, calcType);
                calculator.setArguments(values[1]);
                targetColumn = calculator.calc();
                if (targetColumn == null) {
                    return null;
                }
                targetColumn.setName(targetColumnName);
                break;

            case SRC:
            case TAR:
            case MAP:
                String[] dataTableParameters = sourceColumnName.split("[.]");
                value = converter.valuesFromDataTable(dataTableParameters[0], dataTableParameters[1]);
                if (value == null) {
                    return null;
                }
                targetColumn = application.createDataColumn(sourceColumnType.name(), Types.VARCHAR, value);
                break;

            case VAR:
                SystemVariable systemVariable = SystemVariable.parse(sourceColumnTypeArg);
                if (systemVariable == null) {
                    progressBar.close();
                    error("Invalid name({}) for system variable of target column({})", sourceColumnName, targetColumnName);
                    return null;
                }
                targetColumn = application.systemVariableMap.get(systemVariable);
                targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                break;

            case ARG:
                int argIndex;
                try {
                    argIndex = Integer.parseInt(sourceColumnTypeArg) - 1;
                } catch (Exception ex) {
                    log.warn("{}, invalid argument index, first argument is returned", sourceColumnName);
                    argIndex = 0;
                }
                String[] args = application.args;
                if (argIndex < 0) {
                    log.warn("{}, invalid argument index({}), argument index is start at 1", sourceColumnName, argIndex + 1);
                    argIndex = 0;
                } else if (argIndex > args.length) {
                    log.warn("{}, invalid argument index({}), last argument index is {}", sourceColumnName, args.length);
                    argIndex = args.length - 1;
                }
                targetColumn = application.createDataColumn(sourceColumnName, Types.VARCHAR, args[argIndex]);
                break;

            case NON:
                targetColumn = sourceRow.getColumn(sourceColumnName);
                if (targetColumn == null) {
                    progressBar.close();
                    error("No column({}) in source({}) that required by target({})", sourceColumnName, sourceName, name);
                    return null;
                }

                targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                break;

            case INV:
                progressBar.close();
                error("Invalid source-column({}) for target-column({})", sourceColumnName, targetColumnName);
                return null;

            default: // constant for STR, INT, DTE, DTT, DEC
                if (sourceColumnTypeArg.compareToIgnoreCase("NULL") == 0) {
                    sourceColumnTypeArg = null;
                } else if (sourceColumnTypeArg.contains("$[")) {
                    sourceColumnTypeArg = converter.compileDynamicValues(sourceColumnTypeArg);
                }

                targetColumn = application.createDataColumn(targetColumnName, sourceColumnType.getDataType(), sourceColumnTypeArg);
                if (targetColumn == null) {
                    progressBar.close();
                    error("Invalid constant({}) for {} that required by target column({})", sourceColumnTypeArg, sourceColumnType.name(), targetColumnName);
                    return null;
                }

                if (targetColumn.getType() == Types.VARCHAR) {
                    value = targetColumn.getValue();
                    value = converter.compileDynamicValues(value);
                    targetColumn.setValue(value);
                }

        }// end of switch(sourceColumnType)

        return targetColumn;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Target.class);
    }

    public TargetConfig getTargetConfig() {
        return targetConfig;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public List<DataTable> getMappingTableList() {
        return mappingTableList;
    }

}
