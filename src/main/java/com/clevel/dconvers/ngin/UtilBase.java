package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public abstract class UtilBase extends AppBase {

    public UtilBase(Application application, String name) {
        super(application, name);
    }

    /**
     * all index in parameters start at 0.
     */
    protected List<Integer> createIndexList(String[] arguments, int firstArgument, int minIndex, int maxIndex) {
        List<Integer> indexList = new ArrayList<>();
        String argument;
        int index;
        int last;

        for (int argIndex = firstArgument; argIndex < arguments.length; argIndex++) {
            argument = arguments[argIndex];
            if (argument.indexOf("-") > 0) {
                String[] range = argument.split("[-]");
                index = Integer.valueOf(range[0]) - 1;
                last = Integer.valueOf(range[1]) - 1;
                if (last > maxIndex) {
                    last = maxIndex;
                }

                while (index <= last) {
                    indexList.add(index);
                    index = index + 1;
                }
            } else {
                index = Integer.valueOf(argument) - 1;
                if (index < minIndex) {
                    continue;
                } else if (index > maxIndex) {
                    break;
                }
                indexList.add(index);
            }
        }

        log.debug("UtilBase.createIndexList(args:{}, firstArg:{}, minIndex:{}, maxIndex:{}) => {}", arguments, firstArgument, minIndex, maxIndex, indexList);
        return indexList;
    }

    protected String getFirstValue(String commaSeparatedValues) {
        int index = commaSeparatedValues.indexOf(",");
        if (index < 0) {
            return commaSeparatedValues;
        }

        return commaSeparatedValues.substring(0, index);
    }

    protected DataRow insertReplaceColumn(DataRow existingRow, String newColumnName, int newColumnIndex, DataColumn value) {

        DataRow newRow = new DataRow(existingRow.getDataTable());
        List<DataColumn> columnList = newRow.getColumnList();
        columnList.addAll(existingRow.getColumnList());

        DataColumn dataColumn;
        if (Property.REPLACE.key().equalsIgnoreCase(newColumnName)) {
            dataColumn = columnList.get(newColumnIndex);
            newColumnName = dataColumn.getName();
            value.setName(newColumnName);
            columnList.remove(dataColumn);
        }

        try {
            columnList.add(newColumnIndex, value);
        } catch (Exception ex) {
            log.error("Invalid columnIndex({}) please find and fix this config-value({}:{}), error-detail({})", newColumnIndex, newColumnName, newColumnIndex + 1, ex.getMessage());
            return null;
        }

        newRow.updateColumnMap();
        return newRow;

    }

}
