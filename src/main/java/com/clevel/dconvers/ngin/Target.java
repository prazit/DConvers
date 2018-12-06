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
import java.util.List;
import java.util.Map;

public class Target extends AppBase {

    private Converter converter;
    private TargetConfig targetConfig;

    private String sourceName;
    private Source source;

    private DataTable dataTable;
    private DataTable mappingTable;

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

        sourceName = targetConfig.getSource();
        source = converter.getSource(sourceName);

        return true;
    }

    @Override
    public boolean validate() {

        if (source == null) {
            log.error("Source({}) is not found, required by Target({})", sourceName, name);
            return false;
        }

        return true;

    }

    private String getMappingTableName(String prefix, String sourceTableName, String targetTableName) {
        return prefix + sourceTableName + "_to_" + targetTableName;
    }

    public boolean buildDataTable() {
        log.trace("Target({}).buildDataTable.", name);

        List<Pair<String, String>> columnList = targetConfig.getColumnList();
        String sourceName = targetConfig.getSource();
        DataTable sourceDataTable = source.getDataTable();
        String sourceIdColumnName = sourceDataTable.getIdColumnName();

        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;
        String targetIdColumnName = targetConfig.getId();

        String mappingTablePrefix = dataConversionConfigFile.getMappingTablePrefix();
        String mappingTableName = getMappingTableName(mappingTablePrefix, sourceName, name);
        String mappingSourceIdColumnName = Property.SOURCE_ID.key();
        String mappingTargetIdColumnName = Property.TARGET_ID.key();

        dataTable = new DataTable(name, targetIdColumnName, targetConfig.getOutputConfig().getSqlPostSQL(), this);
        converter.setCurrentTable(dataTable);

        mappingTable = new DataTable(mappingTableName, mappingTargetIdColumnName);
        mappingTable.setOwner(dataTable);

        Map<SystemVariable, DataColumn> systemVars = application.systemVariableMap;
        DataLong varRowNumber = (DataLong) systemVars.get(SystemVariable.ROW_NUMBER);
        varRowNumber.setValue(targetConfig.getRowNumberStartAt() - 1);

        DynamicValueType sourceColumnType;
        DataColumn targetColumn;
        String sourceColumnName;
        String targetColumnName;
        DataRow targetRow;
        DataRow mappingRow;
        int targetColumnIndex;

        List<DataRow> sourceRowList = sourceDataTable.getAllRow();
        int rowCount = sourceRowList.size();
        ProgressBar progressBar;
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
            targetRow = new DataRow(dataTable);
            targetColumnIndex = 0;
            for (Pair<String, String> columnPair : columnList) {
                targetColumnIndex++;
                targetColumnName = columnPair.getKey();
                sourceColumnName = columnPair.getValue();

                sourceColumnType = parseDynamicValueType(sourceColumnName);
                switch (sourceColumnType) {
                    case NON:
                        targetColumn = sourceRow.getColumn(sourceColumnName);
                        if (targetColumn == null) {
                            progressBar.close();
                            log.error("No column({}) in source({}) that required by target({})", sourceColumnName, source.getName(), name);
                            return false;
                        }

                        targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                        break;

                    case VAR:
                        SystemVariable systemVariable = SystemVariable.parse(sourceColumnName);
                        if (systemVariable == null) {
                            progressBar.close();
                            log.error("Invalid name({}) for system variable of target column({})", sourceColumnName, targetColumnName);
                            return false;
                        }
                        targetColumn = application.systemVariableMap.get(systemVariable);
                        targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                        break;

                    case ARG:
                        int argIndex;
                        try {
                            argIndex = Integer.parseInt(sourceColumnName.substring(4)) - 1;
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

                    case COL:
                        String[] sourceColumnNames = sourceColumnName.split(">>");
                        sourceColumnName = sourceColumnNames[0];
                        targetColumn = sourceRow.getColumn(sourceColumnName);
                        if (targetColumn == null) {
                            progressBar.close();
                            log.error("No column({}) in source({}) that required by target({})", sourceColumnName, source.getName(), name);
                            log.debug("source({}) has following columns {}", source.getName(), sourceRow);
                            return false;
                        }

                        String[] mappings = sourceColumnNames[1].split("[.]");

                        DataTable asSourceTable = converter.getDataTable(mappings[0]);
                        if (asSourceTable == null) {
                            progressBar.close();
                            log.error("No table({}) in converter({}) that required by target({})", mappings[0], converter.getName(), name);
                            return false;
                        }

                        sourceColumnName = mappings[1];
                        DataRow asSourceRow = asSourceTable.getRow(sourceColumnName, targetColumn.getValue());
                        if (asSourceRow == null) {
                            progressBar.close();
                            log.error("No row contains column({}) with value({}) in a table({}) in converter({}) that required by target({})", sourceColumnName, targetColumn.getValue(), asSourceTable.getTableName(), converter.getName(), name);
                            log.debug("asSourceTable = {}", asSourceTable);
                            return false;
                        }

                        sourceColumnName = sourceColumnNames[2];
                        targetColumn = asSourceRow.getColumn(sourceColumnName);
                        if (targetColumn == null) {
                            progressBar.close();
                            log.error("No column({}) in data-table({}) in converter({}) that required by target({})", sourceColumnName, mappings[0], converter.getName(), name);
                            return false;
                        }

                        targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                        break;

                    case TXT:
                        String value = converter.valueFromFile(sourceColumnName);
                        if (value == null) {
                            return false;
                        }
                        targetColumn = application.createDataColumn(DynamicValueType.TXT.name(), Types.VARCHAR, value);
                        break;

                    case CAL:
                        String[] values = sourceColumnName.split("[()]");
                        CalcTypes calcType = CalcTypes.parse(values[0]);
                        if (calcType == null) {
                            log.error("Invalid Calculator({}) for target column({})", sourceColumnName, targetColumnName);
                            return false;
                        }
                        Calc calculator = CalcFactory.getCalc(application, calcType);
                        calculator.setArguments(values[1]);
                        value = calculator.calc();
                        if (value == null) {
                            return false;
                        }
                        targetColumn = application.createDataColumn(DynamicValueType.CAL.name(), Types.VARCHAR, value);
                        break;

                    case SRC:
                    case TAR:
                    case MAP:
                        String[] dataTableParameters = sourceColumnName.split("[.]");
                        value = converter.valuesFromDataTable(dataTableParameters[0], dataTableParameters[1]);
                        if (value == null) {
                            return false;
                        }
                        targetColumn = application.createDataColumn(sourceColumnType.name(), Types.VARCHAR, value);
                        break;

                    case INV:
                        progressBar.close();
                        log.error("Invalid source-column({}) for target-column({})", sourceColumnName, targetColumnName);
                        return false;

                    default: // constants
                        sourceColumnName = sourceColumnName.substring(4);
                        if (sourceColumnName.compareToIgnoreCase("NULL") == 0) {
                            sourceColumnName = null;
                        }

                        targetColumn = application.createDataColumn(targetColumnName, sourceColumnType.getDataType(), sourceColumnName);
                        if (targetColumn == null) {
                            progressBar.close();
                            log.error("Invalid constant({}) for {} that required by target column({})", sourceColumnName, sourceColumnType.name(), targetColumnName);
                            return false;
                        }

                }// end of switch(sourceColumnType)
                targetRow.putColumn(targetColumnName, targetColumn);

            } // end of for(ColumnPair)
            dataTable.addRow(targetRow);

            // -- start mapping table

            mappingRow = new DataRow(mappingTable);

            targetColumn = targetRow.getColumn(targetIdColumnName);
            if (targetColumn == null) {
                progressBar.close();
                log.error("Invalid target id({}) for target({}) that required by mapping table({})", targetIdColumnName, name, mappingTableName);
                return false;
            }
            mappingRow.putColumn(mappingTargetIdColumnName, targetColumn.clone(1, mappingTargetIdColumnName));

            targetColumn = sourceRow.getColumn(sourceIdColumnName);
            if (targetColumn == null) {
                progressBar.close();
                log.error("Invalid source id({}) for source({}) that required by mapping table({})", sourceIdColumnName, source.getName(), mappingTableName);
                return false;
            }
            mappingRow.putColumn(mappingSourceIdColumnName, targetColumn.clone(1, mappingSourceIdColumnName));

            mappingTable.addRow(mappingRow);

        } // end of for(DataRow)
        progressBar.close();

        log.info("TAR:{} has {} row(s)", name, dataTable.getRowCount());
        log.info("MAP:{} has {} row(s)", mappingTableName, dataTable.getRowCount());

        if (log.isDebugEnabled()) {
            if (dataTable.getRowCount() > 10) {
                log.debug("buildDataTable({}). targetTable has {} rows, firstRow is {}", name, dataTable.getRowCount(), dataTable.getRow(0));
            } else {
                log.debug("buildDataTable({}). targetTable has {} rows following {}", name, dataTable.getRowCount(), dataTable);
            }
            if (mappingTable.getRowCount() > 10) {
                log.debug("buildDataTable({}). mappingTable has {} rows, firstRow is {}", name, mappingTable.getRowCount(), mappingTable.getRow(0));
            } else {
                log.debug("buildDataTable({}). mappingTable has {} rows following {}", name, mappingTable.getRowCount(), mappingTable);
            }
        }

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

        char ch = sourceColumnName.charAt(3);
        if (ch != ':') {
            if (sourceColumnName.indexOf(">>") >= 0) {
                return DynamicValueType.COL;
            }
            return DynamicValueType.NON;
        }

        String keyWord = sourceColumnName.substring(0, 3).toUpperCase();
        DynamicValueType sourceColumn;
        try {
            sourceColumn = DynamicValueType.valueOf(keyWord);
        } catch (IllegalArgumentException e) {
            return DynamicValueType.INV;
        }

        return sourceColumn;
    }

    private DataColumn getTargetColumn(String mappingTablePrefix, String sourceName, DataColumn sourceIdColumn, String targetName, String targetColumnName) {
        Target target = converter.getTarget(targetName);
        if (target == null) {
            return null;
        }

        DataTable targetTable = target.getDataTable();
        String mappingTableName = getMappingTableName(mappingTablePrefix, sourceName, targetTable.getTableName());

        DataTable mappingTable = target.getMappingTable();
        if (!mappingTable.getTableName().equals(mappingTableName)) {
            log.error("No mapping table({}) between source({}) and target({})", mappingTableName, sourceName, target.getName());
            return null;
        }

        String sourceId = sourceIdColumn.getValue();
        DataRow mappingRow = mappingTable.getRow(sourceId);
        DataColumn targetIdColumn = mappingRow.getColumn(Property.TARGET_ID.key());

        DataRow targetRow = targetTable.getRow(targetIdColumn.getValue());
        DataColumn targetColumn = targetRow.getColumn(targetColumnName);
        if (targetColumn == null) {
            log.error("No column({}) in target({})", targetColumnName, targetName);
            return null;
        }

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

    public DataTable getMappingTable() {
        return mappingTable;
    }

}
