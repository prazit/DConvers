package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RowFilterTransform extends Transform {

    private enum FilterType {
        INCLUDE, EXCLUDE;
    }

    public RowFilterTransform(Application application, String name) {
        super(application, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        // rowfilter([FilterType],[ColumnIdentifier]=value)
        String argumentString = getArgument(Property.ARGUMENTS.key());
        String[] arguments = argumentString.split(",");
        if (arguments.length < 2) {
            error("Invalid arguments for transform=ROWFILTER({})", argumentString);
            return false;
        }

        FilterType filterType;
        try {
            filterType = FilterType.valueOf(arguments[0].toUpperCase());
        } catch (Exception ex) {
            error("Invalid filterType({}) for transform=ROWFILTER({}) : {}", arguments[0], argumentString, ex.getMessage());
            return false;
        }

        String[] columnAndValue = arguments[1].split("=");
        String columnName = columnAndValue[0];
        String value = columnAndValue[1];
        log.debug("ROWFILTER(filterType:{},columnName:{},value:{})", filterType, columnName, value);

        List<DataRow> newRowList = new ArrayList<>();
        List<DataRow> rowList = dataTable.getRowList();
        String dataColumnValue;
        DataColumn dataColumn;

        boolean condition;
        boolean isNullValue = value.toUpperCase().equals("NULL");
        boolean isInclude = FilterType.INCLUDE.equals(filterType);

        if (isInclude) {

            for (DataRow row : rowList) {
                dataColumn = row.getColumn(columnName);
                dataColumnValue = dataColumn.getValue();

                if (dataColumnValue == null) {
                    /* allow: include(column=null) */
                    if (isNullValue) {
                        newRowList.add(row);
                    } else {
                        log.debug("rowfilter remove row({}={}):{}", columnName, dataColumnValue, row);
                    }
                    continue;
                }

                condition = dataColumnValue.compareTo(value) == 0;
                if (condition) {
                    newRowList.add(row);
                } else {
                    log.debug("rowfilter remove row({}={}):{}", columnName, dataColumnValue, row);
                }
            }

        } else { // (isExclude)

            for (DataRow row : rowList) {
                dataColumn = row.getColumn(columnName);
                dataColumnValue = dataColumn.getValue();

                if (dataColumnValue == null) {
                    /* allow: include(column=null) */
                    if (!isNullValue) {
                        newRowList.add(row);
                    } else {
                        log.debug("rowfilter remove row({}={}):{}", columnName, dataColumnValue, row);
                    }
                    continue;
                }

                condition = dataColumnValue.compareTo(value) == 0;
                if (!condition) {
                    newRowList.add(row);
                } else {
                    log.debug("rowfilter remove row({}={}):{}", columnName, dataColumnValue, row);
                }
            }

        }

        dataTable.setRowList(newRowList);
        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(RowFilterTransform.class);
    }
}
