package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class COLValue extends DynamicValue {

    private Converter converter;
    private boolean isSingleColumn;

    private String valueColumnName;
    private DynamicValue notfoundValue;

    private boolean isCurrentTable;
    private DataTable lookupTable;

    /*for single column lookup*/
    private DynamicValue lookupValue;
    private String lookupColumnName;
    private String lookupData;

    /*for multiple columns lookup*/
    private List<DynamicValue> lookupValueList;
    private List<String> lookupColumnNameList;
    private List<String> lookupDataList;

    public COLValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
        lookupValue = null;
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        converter = application.currentConverter;

        // sourceColumnName contains 'lookupValueDef>>lookupTable.lookupColumnName>>valueColumnName,notFoundValue'
        // example: column.target_column_name=STR:OK,VAR:EMPTY_STRING>>SRC:OK_OR_NOT.state,empty>>description
        // when notFoundValue is not defined, error will be created when the lookupValue is not found in a lookupTable.
        String[] sourceColumnNames = sourceColumnName.split(">>");
        if (sourceColumnNames.length < 3) {
            valid = false;
            error("Invalid syntax for column.{}={}", name, sourceColumnName);
            return;
        }

        isSingleColumn = !sourceColumnNames[0].contains(",");
        if (isSingleColumn) {
            prepareSingleColumn(sourceName, sourceColumnNames, sourceColumnArg);
        } else {
            prepareMultipleColumns(sourceName, sourceColumnNames, sourceColumnArg);
        }
    }

    private void prepareSingleColumn(String sourceName, String[] sourceColumnNames, String sourceColumnArg) {
        // sourceColumnNames contains 3 components like this: 'lookupValue' >> 'lookupTable.lookupColumnName' >> 'valueColumnName,notFoundValue'

        // Lookup Value: convert to DynamicValue.
        String lookupValue = sourceColumnNames[0];
        this.lookupValue = getDynamicValue(sourceName, lookupValue);

        // Lookup Table and Lookup Column
        String[] mappings = sourceColumnNames[1].split("[.]");
        if (mappings.length < 2) {
            valid = false;
            error("Invalid syntax for column.{}={}:{}", name, lookupValue, sourceColumnArg);
            return;
        }

        // Lookup Table
        if (!setLookupTable(mappings[0])) {
            return;
        }

        // Lookup Column
        lookupColumnName = mappings[1];

        // Value Column Name
        setValueColumnName(sourceName, sourceColumnNames[2].split("[,]"));
    }

    private void setValueColumnName(String sourceName, String[] valueColumns) {
        valueColumnName = valueColumns[0];

        // Notfound Value
        String notFoundValueDef = (valueColumns.length > 1) ? valueColumns[1] : null;
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
    }

    private void prepareMultipleColumns(String sourceName, String[] sourceColumnNames, String sourceColumnArg) {
        // sourceColumnNames contains 3 components like this: 'lookupValue1,lookupValue2' >> 'lookupTable.lookupColumnName1,lookupColumnName2' >> 'valueColumnName,notFoundValue'

        // Lookup Value
        String[] lookupValues = sourceColumnNames[0].split("[,]");
        lookupValueList = new ArrayList<>();
        for (String lookupValue : lookupValues) {
            lookupValueList.add(getDynamicValue(sourceName, lookupValue));
        }

        // Lookup Table and Lookup Column
        String[] lookupColumnNames = sourceColumnNames[1].split("[,]");
        if (lookupColumnNames.length != lookupValues.length) {
            valid = false;
            error("Number not match for lookup values and lookup column names, invalid syntax for column.{}={}:{}", name, lookupValue, sourceColumnArg);
            return;
        }
        String[] mappings = lookupColumnNames[0].split("[.]");
        if (mappings.length < 2) {
            valid = false;
            error("Invalid syntax for column.{}={}:{}", name, lookupValue, sourceColumnArg);
            return;
        }

        // Lookup Table
        if (!setLookupTable(mappings[0])) {
            return;
        }

        // Lookup Column
        lookupColumnNameList = new ArrayList<>();
        lookupColumnNameList.add(mappings[1]);
        lookupColumnNameList.addAll(Arrays.asList(lookupColumnNames).subList(1, lookupColumnNames.length));

        // Value Column Name
        setValueColumnName(sourceName, sourceColumnNames[2].split("[,]"));
    }

    private boolean setLookupTable(String lookupTableName) {
        if (lookupTableName.equalsIgnoreCase("CURRENT")) {
            isCurrentTable = true;
        } else {
            isCurrentTable = false;
            lookupTable = converter.getDataTable(lookupTableName);
            if (lookupTable == null) {
                valid = false;
                error("No table({}) in converter({}) that required by target({}.{})", lookupTableName, converter.getName(), targetName, name);
                return false;
            }
        }
        return true;
    }

    private DynamicValue getDynamicValue(String sourceName, String lookupValue) {
        DynamicValue dynamicValue;

        if (lookupValue.indexOf(":") > 0) {
            DynamicValueType sourceColumnType2 = DynamicValueType.parseTargetColumn(lookupValue);
            String sourceColumnTypeArg2 = lookupValue.length() > 4 ? lookupValue.substring(4) : "";
            dynamicValue = DynamicValueFactory.getDynamicValue(sourceColumnType2, application, targetName, name, targetColumnIndex);
            dynamicValue.prepare(sourceName, lookupValue, sourceColumnType2, sourceColumnTypeArg2);
        } else {
            dynamicValue = new NONValue(application, targetName, lookupValue, targetColumnIndex);
            dynamicValue.prepare(sourceName, lookupValue, DynamicValueType.NON, "");
        }

        return dynamicValue;
    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        if (!isValid()) {
            return null;
        }

        if (isSingleColumn) {
            return getValueBySingleColumn(sourceRow);
        } else {
            return getValueByMultipleColumns(sourceRow);
        }
    }

    private DataColumn getValueBySingleColumn(DataRow sourceRow) {
        if (isCurrentTable) {
            lookupTable = converter.getCurrentTable();
        }

        DataColumn lookupData = lookupValue.getValue(sourceRow);
        if (lookupData == null) {
            return null;
        }
        this.lookupData = lookupData.getValue();

        return getValueByDataRow(sourceRow, lookupTable.getRow(lookupColumnName, this.lookupData));
    }

    private DataColumn getValueByMultipleColumns(DataRow sourceRow) {
        if (isCurrentTable) {
            lookupTable = converter.getCurrentTable();
        }

        lookupDataList = new ArrayList<>();
        DataColumn lookupData;
        for (DynamicValue lookupValue : lookupValueList) {
            lookupData = lookupValue.getValue(sourceRow);
            if (lookupData == null) {
                return null;
            }
            lookupDataList.add(lookupData.getValue());
        }

        return getValueByDataRow(sourceRow, lookupTable.getRow(lookupColumnNameList, lookupDataList));
    }

    private DataColumn getValueByDataRow(DataRow sourceRow, DataRow dataRow) {
        if (dataRow == null) {
            if (notfoundValue == null) {
                if (isSingleColumn) {
                    error("No row contains column({}) with value({}) in a table({}) in converter({}) that required by target({}.{})", lookupColumnName, lookupData, lookupTable.getName(), converter.getName(), targetName, name);
                }else{
                    error("No row contains column({}) with value({}) in a table({}) in converter({}) that required by target({}.{})", lookupColumnNameList.toArray(), lookupDataList.toArray(), lookupTable.getName(), converter.getName(), targetName, name);
                }
                log.debug("lookupTable = {}", lookupTable);
                return null;
            }
            return notfoundValue.getValue(sourceRow);
        }

        DataColumn dataColumn = dataRow.getColumn(valueColumnName);
        if (dataColumn == null) {
            error("No column({}) in data-table({}) in converter({}) that required by target({}.{})", valueColumnName, lookupTable.getName(), name, converter.getName(), targetName, name);
            return null;
        }

        return dataColumn.clone(targetColumnIndex, name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(COLValue.class);
    }

    public DataTable getLookupTable() {
        return lookupTable;
    }
}
