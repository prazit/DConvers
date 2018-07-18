package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.data.*;
import javafx.util.Pair;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Target extends AppBase {

    private Converter converter;
    private TargetConfig targetConfig;
    private Map<String, Source> sourceMap;

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
        sourceMap = converter.getSourceMap();
        return true;
    }

    @Override
    public boolean validate() {

        if (sourceMap == null || sourceMap.size() == 0) {
            log.error("Sources are required for target({})", name);
            return false;
        }

        String sourceName = targetConfig.getSource();
        if (!sourceMap.containsKey(sourceName)) {
            log.error("Source({}) is not found, required by Target({})", sourceName, name);
            return false;
        }

        return true;

    }

    public boolean buildDataTable() {

        List<Pair<String, String>> columnList = targetConfig.getColumnList();
        Source source = sourceMap.get(targetConfig.getSource());
        DataTable sourceDataTable = source.getDataTable();
        String sourceId = sourceDataTable.getIdColumnName();

        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;
        String targetTableName = targetConfig.getTable();
        String targetId = targetConfig.getId();

        String mappingTableName = dataConversionConfigFile.getMappingTablePrefix() + sourceDataTable.getTableName() + "_to_" + targetTableName;
        String mappingId = Property.ID.key();

        int mappingRowNumber = 0;
        mappingTable = new DataTable(mappingTableName);
        mappingTable.setIdColumnName(mappingId);

        dataTable = new DataTable(targetTableName);
        dataTable.setIdColumnName(targetId);

        Map<SystemVariable, DataColumn> systemVars = application.systemVariableMap;
        DataLong varRowNumber = (DataLong) systemVars.get(SystemVariable.ROWNUMBER);
        DataDate varNow = (DataDate) systemVars.get(SystemVariable.NOW);

        varRowNumber.setValue(targetConfig.getRowNumberStartAt() - 1);
        varNow.setValue(new Date());

        SourceColumnType sourceColumnType;
        DataColumn targetColumn;
        String sourceColumnName;
        String sourceIdColumnName;
        String targetColumnName;
        String targetIdColumnName;
        DataRow targetRow;
        DataRow mappingRow;
        int targetColumnIndex;

        List<DataRow> sourceRowList = sourceDataTable.getAllRow();
        int rowCount = sourceRowList.size();
        ProgressBar progressBar;
        if (rowCount > 3000) {
            progressBar = new ProgressBar("Build target(" + name + ")", rowCount, 500, System.out, ProgressBarStyle.ASCII, "K", 1000);
        } else {
            progressBar = new ProgressBar("Build target(" + name + ")", rowCount, 500, System.out, ProgressBarStyle.ASCII, " rows", 1);
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

                sourceColumnType = parseSourceColumnType(sourceColumnName);
                switch (sourceColumnType) {
                    case NON:
                        targetColumn = sourceRow.getColumn(sourceColumnName);
                        if (targetColumn == null) {
                            log.error("No column({}) in source({}) that required by target({})", sourceColumnName, source.getName(), name);
                            return false;
                        }
                        targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                        break;

                    case VAR:
                        sourceColumnName = sourceColumnName.substring(4).toUpperCase();
                        targetColumn = systemVars.get(SystemVariable.valueOf(sourceColumnName));
                        if (targetColumn == null) {
                            log.error("Invalid name({}) for system variable of target column({})", sourceColumnName, targetColumnName);
                            return false;
                        }
                        targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                        break;

                    case SRC:
                        sourceColumnName = sourceColumnName.substring(4);
                        targetColumn = sourceRow.getColumn(sourceColumnName);
                        if (targetColumn == null) {
                            log.error("No column({}) in source({}) that required by target({})", sourceColumnName, source.getName(), name);
                            return false;
                        }
                        targetColumn = targetColumn.clone(targetColumnIndex, targetColumnName);
                        break;

                    case INV:
                        log.error("Invalid source column({}) for target column({})", sourceColumnName, targetColumnName);
                        return false;

                    default: // constants
                        sourceColumnName = sourceColumnName.substring(4);
                        targetColumn = application.createDataColumn(targetColumnName, sourceColumnType.getDataType(), sourceColumnName);
                        if (targetColumn == null) {
                            log.error("Invalid constant({}) for {} that required by target column({})", sourceColumnName, sourceColumnType.name(), targetColumnName);
                            return false;
                        }
                }// end of switch(sourceColumnType)
                targetRow.putColumn(targetColumnName, targetColumn);

            } // end of for(ColumnPair)
            dataTable.addRow(targetRow);

            // -- start mapping table

            mappingRowNumber++;
            mappingRow = new DataRow(mappingTable);

            targetColumn = application.createDataColumn(mappingId, Types.BIGINT, String.valueOf(mappingRowNumber));
            if (targetColumn == null) {
                log.error("Invalid mapping id({}) that required by mapping table({})", mappingId, mappingTableName, mappingTableName);
                return false;
            }
            mappingRow.putColumn(mappingId, targetColumn);

            targetColumn = sourceRow.getColumn(sourceId);
            if (targetColumn == null) {
                log.error("Invalid source id({}) for source{} that required by mapping table({})", sourceId, source.getName(), mappingTableName);
                return false;
            }
            sourceIdColumnName = Property.SOURCE_ID.key();
            mappingRow.putColumn(sourceIdColumnName, targetColumn.clone(1, sourceIdColumnName));

            targetColumn = targetRow.getColumn(targetId);
            if (targetColumn == null) {
                log.error("Invalid target id({}) for target{} that required by mapping table({})", targetId, name, mappingTableName);
                return false;
            }
            targetIdColumnName = Property.TARGET_ID.key();
            mappingRow.putColumn(targetIdColumnName, targetColumn.clone(1, targetIdColumnName));

            mappingTable.addRow(mappingRow);

        } // end of for(DataRow)
        progressBar.close();

        // TODO: create reportRow and add into the application.reportTable

        if (log.isDebugEnabled()) {
            if (dataTable.getRowCount() > 100) {
                log.debug("buildDataTable({}). targetTable has {} rows, firstRow is {}", name, dataTable.getRowCount(), dataTable.getRow(0));
            } else {
                log.debug("buildDataTable({}). targetTable has {} rows following {}", name, dataTable.getRowCount(), dataTable);
            }
            if (mappingTable.getRowCount() > 100) {
                log.debug("buildDataTable({}). mappingTable has {} rows, firstRow is {}", name, mappingTable.getRowCount(), mappingTable.getRow(0));
            } else {
                log.debug("buildDataTable({}). mappingTable has {} rows following {}", name, mappingTable.getRowCount(), mappingTable);
            }
        }

        return true;
    }

    private SourceColumnType parseSourceColumnType(String sourceColumnName) {
        if (sourceColumnName.length() < 5) {
            return SourceColumnType.NON;
        }

        char ch = sourceColumnName.charAt(3);
        if (ch != ':') {
            return SourceColumnType.NON;
        }

        String keyWord = sourceColumnName.substring(0, 3).toUpperCase();
        SourceColumnType sourceColumn;
        try {
            sourceColumn = SourceColumnType.valueOf(keyWord);
        } catch (IllegalArgumentException e) {
            return SourceColumnType.INV;
        }
        if (sourceColumn == null) {
            return SourceColumnType.INV;
        }

        return sourceColumn;
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
