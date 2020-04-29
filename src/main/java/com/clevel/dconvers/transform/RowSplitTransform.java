package com.clevel.dconvers.transform;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class RowSplitTransform extends Transform {

    public RowSplitTransform(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    public boolean transform(DataTable toDataTable) {

        // rowsplit([ColumnIndex],[regex],[DataType])
        String argumentString = getArgument(Property.ARGUMENTS.key());
        String[] arguments = argumentString.split(",");

        String columnIdentifier = arguments.length < 1 ? Defaults.ROW_SPLIT_COLUMN_IDENTIFIER.getStringValue() : arguments[0];
        String regex = arguments.length < 2 ? Defaults.ROW_SPLIT_REGEX.getStringValue() : arguments[1];
        DynamicValueType dynamicValueType = arguments.length < 3 ? DynamicValueType.STR : DynamicValueType.parse(arguments[2]);
        int dataType = dynamicValueType == null ? DynamicValueType.STR.getDataType() : dynamicValueType.getDataType();
        Converter converter = dconvers.currentConverter;

        DataColumn dataColumn;
        String columnName;
        String value;
        String[] values;
        String newValue;
        List<DataRow> newRowList = new ArrayList<>();
        DataRow newRow;
        DataColumn newColumn;
        for (DataRow dataRow : toDataTable.getRowList()) {
            dataColumn = converter.getDataColumn(columnIdentifier, dataRow);
            if (dataColumn == null) {
                error("Invalid one or more identifiers for the RowSplit transform, please check your parameters.");
                return false;
            }

            dataColumn.setNullString(null);
            value = dataColumn.getValue();
            if (value == null) {
                // skip row with null value
                continue;
            }

            value = value.trim();
            if (value.length() == 0) {
                // skip row with empty string
                continue;
            }

            try {
                values = value.split(regex);
            } catch (PatternSyntaxException exception) {
                error("Invalid regex argument for RowSplit transform.");
                return false;
            }

            // update current row
            newRowList.add(dataRow);
            if (dataColumn.getType() == dataType) {
                dataColumn.setValue(values[0]);
            } else {
                columnName = dataColumn.getName();
                dataColumn = dconvers.createDataColumn(columnName, dataType, values[0]);
                dataRow.putColumn(columnName, dataColumn);
            }

            // clone current row to new row with new value
            for (int index = 1; index < values.length; index++) {
                newValue = values[index].trim();
                newRow = dataRow.clone();

                newColumn = converter.getDataColumn(columnIdentifier, newRow);
                if (newColumn.getType() == dataType) {
                    newColumn.setValue(newValue);
                } else {
                    columnName = newColumn.getName();
                    newColumn = dconvers.createDataColumn(columnName, dataType, newValue);
                    newRow.putColumn(columnName, newColumn);
                }

                newRowList.add(newRow);
            }
        }

        toDataTable.setRowList(newRowList);
        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(RowSplitTransform.class);
    }
}
