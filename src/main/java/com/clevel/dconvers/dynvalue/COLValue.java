package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class COLValue extends DynamicValue {

    private Converter converter;
    private boolean isCurrentTable;

    private String lookupColumnName;
    private String valueColumnName;
    private DynamicValue lookupValue;
    private DataTable lookupTable;
    private DynamicValue notfoundValue;

    public COLValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
        lookupValue = null;
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        converter = application.currentConverter;

        // sourceColumnName contains 'lookupValueDef>>lookupTable.lookupColumnName>>valueColumnName,notFoundValueDef'
        // example: column.target_column_name=STR:OK>>SRC:OK_OR_NOT.state>>description
        // when notFoundValue is not defined, error will be created when the lookupValue is not found in a lookupTable.
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
                error("No table({}) in converter({}) that required by target({}.{})", mappings[0], converter.getName(), targetName, name);
                return;
            }
        }

        String[] values = sourceColumnNames[2].split("[,]");
        String notFoundValueDef = (values.length > 1) ? values[1] : null;
        if (notFoundValueDef == null) {
            notfoundValue = null;
        } else {
            if (notFoundValueDef.indexOf(":") > 0) {
                DynamicValueType sourceColumnType2 = DynamicValueType.parseTargetColumn(notFoundValueDef);
                String sourceColumnTypeArg2 = notFoundValueDef.length() > 4 ? notFoundValueDef.substring(4) : "";
                notfoundValue = DynamicValueFactory.getDynamicValue(sourceColumnType2, application, targetName, name, targetColumnIndex);
                notfoundValue.prepare(sourceName, notFoundValueDef, sourceColumnType2, sourceColumnTypeArg2);
            } else {
                notfoundValue = null;
            }
        }

        lookupColumnName = mappings[1];
        valueColumnName = values[0];
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
            if (notfoundValue == null) {
                error("No row contains column({}) with value({}) in a table({}) in converter({}) that required by target({}.{})", lookupColumnName, lookupData.getValue(), lookupTable.getName(), converter.getName(), targetName, name);
                log.debug("lookupTable = {}", lookupTable);
                return null;
            }
            return notfoundValue.getValue(sourceRow);
        }

        lookupData = lookupResultRow.getColumn(valueColumnName);
        if (lookupData == null) {
            error("No column({}) in data-table({}) in converter({}) that required by target({}.{})", valueColumnName, lookupTable.getName(), name, converter.getName(), targetName, name);
            return null;
        }

        return lookupData.clone(targetColumnIndex, name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(COLValue.class);
    }
}
