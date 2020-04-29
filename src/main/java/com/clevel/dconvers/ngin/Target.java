package com.clevel.dconvers.ngin;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.conf.TargetConfig;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataLong;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.dynvalue.DynamicValue;
import com.clevel.dconvers.dynvalue.DynamicValueFactory;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.transform.Transform;
import com.clevel.dconvers.transform.TransformFactory;
import com.clevel.dconvers.transform.TransformTypes;
import javafx.util.Pair;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Target extends UtilBase {

    private Converter converter;
    private TargetConfig targetConfig;

    /* store data before transform */
    private DataTable dataTableTransfer;

    /* main data set also store data in realtime changes */
    private DataTable dataTable;

    /* store row from current-source in transfer process */
    private DataRow currentSourceRow;
    private DataRow currentTargetRow;

    private List<String> sourceList;
    private List<DataTable> mappingTableList;
    private List<DynamicValue> dynamicValueList;

    public Target(DConvers dconvers, String name, Converter converter, TargetConfig targetConfig) {
        super(dconvers, name);
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
        currentSourceRow = null;
        currentTargetRow = null;

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
            sourceTableName = "src_" + sourceTableName;
        }
        String name = sourceTableName + "_to_" + targetTableName;
        return name;
    }

    public boolean buildDataTable() {
        log.trace("Target({}).buildDataTable.", name);

        List<Pair<String, String>> columnList = targetConfig.getColumnList();

        String targetIdColumnName = targetConfig.getId();

        String mappingTableName;
        String mappingSourceIdColumnName = Property.SOURCE_ID.key();
        String mappingTargetIdColumnName = Property.TARGET_ID.key();

        dataTable = new DataTable(dconvers, name, targetIdColumnName, this);
        dataTable.setOwner(this);
        converter.setCurrentTable(dataTable);

        dataTableTransfer = dataTable;

        HashMap<SystemVariable, DataColumn> systemVars = dconvers.systemVariableMap;
        DataLong varRowNumber = (DataLong) systemVars.get(SystemVariable.ROW_NUMBER);
        varRowNumber.setValue(targetConfig.getRowNumberStartAt() - 1);

        DynamicValueType sourceColumnType;
        String sourceColumnTypeArg;
        DataColumn targetColumn;
        String sourceColumnName;
        String targetColumnName;
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
            mappingTable = new DataTable(dconvers, mappingTableName, mappingTargetIdColumnName);
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
            if (rowCount == 0) {
                log.debug("Skipped source({}), data not found for target({})", sourceName, name);
                continue;
            }

            progressBar = getProgressBar("Build target(" + name + ")", rowCount);

            dynamicValueList = new ArrayList<>();
            DynamicValue dynamicValue;
            targetColumnIndex = 0;
            for (Pair<String, String> columnPair : columnList) {
                targetColumnIndex++;

                targetColumnName = columnPair.getKey();
                sourceColumnName = columnPair.getValue();
                sourceColumnType = DynamicValueType.parseTargetColumn(sourceColumnName);
                sourceColumnTypeArg = sourceColumnName.length() > 4 ? sourceColumnName.substring(4) : "";

                dynamicValue = DynamicValueFactory.getDynamicValue(sourceColumnType, dconvers, name, targetColumnName, targetColumnIndex);
                if (dynamicValue == null) {
                    error("DynamicValue Creation is failed.");
                    valid = false;
                    return false;
                }
                dynamicValue.setDynamicValueType(sourceColumnType);
                dynamicValue.prepare(sourceName, sourceColumnName, sourceColumnType, sourceColumnTypeArg);
                dynamicValueList.add(dynamicValue);
            }

            boolean firstRow = true;
            for (DataRow sourceRow : sourceRowList) {
                progressBar.step();
                currentSourceRow = sourceRow;

                // -- start target table

                varRowNumber.increaseValueBy(1);
                currentTargetRow = new DataRow(dconvers, dataTable);
                for (DynamicValue columnValue : dynamicValueList) {
                    targetColumn = columnValue.getValue(sourceRow);
                    if (targetColumn == null) {
                        progressBar.close();
                        valid = false;
                        return false;
                    }

                    currentTargetRow.putColumn(targetColumn.getName(), targetColumn);
                    /*if (firstRow) {
                        log.debug("buildTargetColumn({}.{})[{}]", dataTable.getName(), targetColumn.getName(), columnValue.getDynamicValueType());
                    }*/
                }
                dataTable.addRow(currentTargetRow);
                if (firstRow) {
                    firstRow = false;
                }

                // -- start mapping table

                mappingRow = new DataRow(dconvers, mappingTable);

                targetColumn = currentTargetRow.getColumn(targetIdColumnName);
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
            currentSourceRow = null;

            log.info("transferred from source({})", sourceName);
            log.debug("TAR:{} has {} row(s) before transform", name, dataTable.getRowCount());

        } // end of for sourceList

        // -- keep dataTable before transformed for print
        if (targetConfig.getTransferOutputConfig().needOutput() && targetConfig.getTransformConfig().needTransform()) {
            dataTableTransfer = dataTable.clone();
        }

        // -- start transformation
        List<Pair<TransformTypes, HashMap<String, String>>> transformList = targetConfig.getTransformConfig().getTransformList();
        HashMap<String, String> argumentList;
        TransformTypes transformType;
        Transform transform;
        for (Pair<TransformTypes, HashMap<String, String>> transformPair : transformList) {
            transformType = transformPair.getKey();
            argumentList = transformPair.getValue();
            log.trace("transforming Target({}) by Transform({})", name, transformType.name());

            transform = TransformFactory.getTransform(dconvers, transformType);
            transform.setArgumentList(argumentList);
            if (!transform.transform(dataTable)) {
                log.debug("Transform({}) is failed in Target({})", name, transformType.name());
                valid = false;
                return false;
            }
            log.debug("TAR:{} is transformed by {}({}), remain {} row(s) {} column(s)", name, transformType.name(), argumentList, dataTable.getRowCount(), (dataTable.getRowCount() == 0) ? 0 : dataTable.getRow(0).getColumnCount());
        }

        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Target.class);
    }

    public TargetConfig getTargetConfig() {
        return targetConfig;
    }

    public DataRow getCurrentSourceRow() {
        return currentSourceRow;
    }

    public DataRow getCurrentTargetRow() {
        return currentTargetRow;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public DataTable getDataTableBeforeTransform() {
        return dataTableTransfer;
    }

    public List<DataTable> getMappingTableList() {
        return mappingTableList;
    }

    public List<DynamicValue> getDynamicValueList() {
        return dynamicValueList;
    }
}
