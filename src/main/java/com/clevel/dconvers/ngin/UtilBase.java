package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class UtilBase extends AppBase {

    public UtilBase(Application application, String name) {
        super(application, name);
    }

    protected List<Integer> createIndexList(DataRow rowPrototype, String columnIdentifier) {
        List<Integer> indexList = new ArrayList<>();
        int index;
        int last;

        int minIndex = 0;
        int maxIndex = rowPrototype.getColumnCount() - 1;

        if (columnIdentifier.indexOf("-") > 0) {
            /* case: columnIdentifier is range */
            String[] range = columnIdentifier.split("[-]");
            index = Integer.valueOf(range[0]) - 1;
            last = Integer.valueOf(range[1]) - 1;
            if (last > maxIndex) {
                last = maxIndex;
            }

            while (index <= last) {
                indexList.add(index);
                index = index + 1;
            }

        } else if (!NumberUtils.isCreatable(columnIdentifier)) {
            /* case: columnIdentifier is column-name */
            index = rowPrototype.getColumnIndex(columnIdentifier);
            if (index < 0) {
                error("Invalid columnName({}) for table({})", columnIdentifier, rowPrototype.getDataTable().getName());
                return Collections.EMPTY_LIST;
            }
            indexList.add(index);

        } else {
            /* case: columnIdentifier is index number */
            index = Integer.valueOf(columnIdentifier) - 1;
            if (index < minIndex) {
                return Collections.EMPTY_LIST;
            } else if (index > maxIndex) {
                return Collections.EMPTY_LIST;
            }
            indexList.add(index);
        }

        return indexList;
    }

    /**
     * all index in parameters start at 0.
     */
    protected List<Integer> createIndexList(DataRow rowPrototype, String[] arguments, int firstArgument) {
        int minIndex = 0;
        int maxIndex = rowPrototype.getColumnCount() - 1;
        List<Integer> indexList = new ArrayList<>();

        for (int argIndex = firstArgument; argIndex < arguments.length; argIndex++) {
            indexList.addAll(createIndexList(rowPrototype, arguments[argIndex]));
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

        DataRow newRow = new DataRow(application, existingRow.getDataTable());
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
            error("Invalid columnIndex({}) please find and fix this config-value({}:{}), error-detail({})", newColumnIndex, newColumnName, newColumnIndex + 1, ex.getMessage());
            return null;
        }

        newRow.updateColumnMap();
        return newRow;

    }

    protected ProgressBar getProgressBar(String caption, long maxValue) {
        ProgressBar progressBar;
        if (maxValue > Defaults.PROGRESS_SHOW_KILO_AFTER.getLongValue()) {
            progressBar = new me.tongfei.progressbar.ProgressBar(caption, maxValue, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, "K", 1000);
        } else {
            progressBar = new ProgressBar(caption, maxValue, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, " rows", 1);
        }
        progressBar.maxHint(maxValue);
        return progressBar;
    }

}
