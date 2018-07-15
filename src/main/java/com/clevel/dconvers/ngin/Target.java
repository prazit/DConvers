package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String sourceId = source.getSourceConfig().getId();

        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;
        String targetTableName = targetConfig.getTable();
        String targetId = targetConfig.getId();
        String mappingTableName = dataConversionConfigFile.getMappingTablePrefix() + sourceDataTable.getTableName() + "_to_" + targetTableName;

        mappingTable = new DataTable(mappingTableName);
        dataTable = new DataTable(targetTableName);

        Map<SystemVariable, DataColumn> systemVars = application.systemVariableMap;
        DataLong varRowNumber = (DataLong) systemVars.get(SystemVariable.ROWNUMBER);
        DataDate varNow = (DataDate) systemVars.get(SystemVariable.NOW);

        varRowNumber.setValue(0);
        varNow.setValue(new Date());

        SourceColumnType sourceColumnType;
        DataColumn targetColumn;
        String sourceColumnName;
        String targetColumnName;
        DataRow targetRow;
        DataRow mappingRow;

        for (DataRow sourceRow : sourceDataTable.getAllRow()) {

            varRowNumber.increaseValueBy(1);
            targetRow = new DataRow(dataTable);

            for (Pair<String, String> columnPair : columnList) {
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
                        break;

                    case VAR:
                        sourceColumnName = sourceColumnName.substring(4).toUpperCase();
                        targetColumn = systemVars.get(SystemVariable.valueOf(sourceColumnName)).clone();
                        if (targetColumn == null) {
                            log.error("Invalid name({}) for system variable of target column({})", sourceColumnName, targetColumnName);
                            return false;
                        }
                        break;

                    case SRC:
                        sourceColumnName = sourceColumnName.substring(4);
                        targetColumn = sourceRow.getColumn(sourceColumnName);
                        if (targetColumn == null) {
                            log.error("No column({}) in source({}) that required by target({})", sourceColumnName, source.getName(), name);
                            return false;
                        }
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

            mappingRow = new DataRow(mappingTable);

            targetColumn = sourceRow.getColumn(sourceId);
            if (targetColumn == null) {
                log.error("Invalid source id({}) for source{} that required by mapping table({})", sourceId, source.getName(), mappingTable.getTableName());
                return false;
            }
            mappingRow.putColumn(Property.SOURCE_ID.key(), targetColumn);

            targetColumn = targetRow.getColumn(targetId);
            if (targetColumn == null) {
                log.error("Invalid target id({}) for target{} that required by mapping table({})", targetId, name, mappingTable.getTableName());
                return false;
            }
            mappingRow.putColumn(Property.TARGET_ID.key(), targetColumn);

            mappingTable.addRow(mappingRow);

        } // end of for(DataRow)

        // TODO: create reportRow and add into the application.reportTable

        if (log.isDebugEnabled()) {
            if (dataTable.getRowCount() > 100) {
                log.debug("buildDataTable({}). targetTable has {} rows, firstRow is {}", name, dataTable.getRowCount(), dataTable.getRow(0));
            } else {
                log.debug("buildDataTable({}). targetTable has {} rows, {}", name, dataTable.getRowCount(), dataTable);
            }
            if (mappingTable.getRowCount() > 100) {
                log.debug("buildDataTable({}). mappingTable has {} rows, firstRow is {}", name, mappingTable.getRowCount(), mappingTable.getRow(0));
            } else {
                log.debug("buildDataTable({}). mappingTable has {} rows, {}", name, mappingTable.getRowCount(), mappingTable);
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

    public DataTable getDataTable() {
        return dataTable;
    }

    public DataTable getMappingTable() {
        return mappingTable;
    }
}
