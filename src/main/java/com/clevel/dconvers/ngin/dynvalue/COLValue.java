package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class COLValue extends DynamicValue {

    private Converter converter;
    private boolean isCurrentTable;

    private String lookupColumnName;
    private String valueColumnName;
    private DynamicValue lookupValue;
    private DataTable lookupTable;

    public COLValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
        lookupValue = null;
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        converter = application.currentConverter;

        // sourceColumnName contains 'lookupValueDef>>lookupTable.lookupColumnName>>valueColumnName'
        // example: column.target_column_name=STR:OK>>SRC:OK_OR_NOT.state>>description
        String[] sourceColumnNames = sourceColumnName.split(">>");
        if (sourceColumnNames.length < 3) {
            valid = false;
            error("Invalid syntax for column.{}={}", name, sourceColumnName);
            return;
        }

        String lookupValueDef = sourceColumnNames[0];
        if (lookupValueDef.indexOf(":") > 0) {
            DynamicValueType sourceColumnType2 = DynamicValueType.parseTargetColumn(lookupValueDef);
            String sourceColumnTypeArg2 = lookupValueDef.length() > 4 ? lookupValueDef.substring(4) : "";
            lookupValue = DynamicValueFactory.getDynamicValue(sourceColumnType2, application, targetName, name, targetColumnIndex);
            lookupValue.prepare(sourceName, lookupValueDef, sourceColumnType2, sourceColumnTypeArg2);
        } else {
            lookupValue = new NONValue(application, targetName, lookupValueDef, targetColumnIndex);
            lookupValue.prepare(sourceName, lookupValueDef, DynamicValueType.NON, "");
        }

        String[] mappings = sourceColumnNames[1].split("[.]");
        if (mappings.length < 2) {
            valid = false;
            error("Invalid syntax for column.{}={}:{}", name, lookupValueDef, sourceColumnArg);
            return;
        }

        if (mappings[0].equalsIgnoreCase("CURRENT")) {
            isCurrentTable = true;
        } else {
            isCurrentTable = false;
            lookupTable = converter.getDataTable(mappings[0]);
            if (lookupTable == null) {
                valid = false;
                error("No table({}) in converter({})", mappings[0], converter.getName());
                return;
            }
        }

        lookupColumnName = mappings[1];
        valueColumnName = sourceColumnNames[2];
    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        if (!isValid()) {
            return null;
        }

        DataColumn lookupData = lookupValue.getValue(sourceRow);
        if (lookupData == null) {
            return null;
        }

        if (isCurrentTable) {
            lookupTable = converter.getCurrentTable();
        }

        DataRow lookupResultRow = lookupTable.getRow(lookupColumnName, lookupData.getValue());
        if (lookupResultRow == null) {
            error("No row contains column({}) with value({}) in a table({}) in converter({})", lookupColumnName, lookupData.getValue(), lookupTable.getName());
            log.debug("lookupTable = {}", lookupTable);
            return null;
        }

        lookupData = lookupResultRow.getColumn(valueColumnName);
        if (lookupData == null) {
            error("No column({}) in data-table({}) in converter({})", valueColumnName, lookupTable.getName(), name);
            return null;
        }

        return lookupData.clone(targetColumnIndex, name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(COLValue.class);
    }
}
