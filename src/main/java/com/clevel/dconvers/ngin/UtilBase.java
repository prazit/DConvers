package com.clevel.dconvers.ngin;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.FackProgressBar;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ProgressBar;
import com.clevel.dconvers.CLIProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class UtilBase extends AppBase {

    public UtilBase(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    protected List<Integer> createIndexList(DataRow rowPrototype, String columnIdentifier) {
        List<Integer> indexList = new ArrayList<>();
        int index;

        if (columnIdentifier.indexOf("-") > 0) {
            /* case: columnIdentifier is range */
            String[] range = columnIdentifier.split("[-]");
            index = indexOf(rowPrototype, range[0]);
            int last = indexOf(rowPrototype, range[1]);
            while (index <= last) {
                indexList.add(index);
                index = index + 1;
            }
        } else {
            index = indexOf(rowPrototype, columnIdentifier);
            indexList.add(index);
        }

        return indexList;
    }

    protected int indexOf(DataRow rowPrototype, String columnIdentifier) {
        int index;

        if (!NumberUtils.isCreatable(columnIdentifier)) {
            /* case: columnIdentifier is column-name */
            index = rowPrototype.getColumnIndex(columnIdentifier);
            if (index < 0) {
                index = 0;
            }
        } else {
            /* case: columnIdentifier is index number */
            index = Integer.parseInt(columnIdentifier) - 1;
            if (index < 0) {
                index = 0;
            } else if (index >= rowPrototype.getColumnCount()) {
                index = rowPrototype.getColumnCount() - 1;
            }
        }

        return index;
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

        DataRow newRow = new DataRow(dconvers, existingRow.getDataTable());
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
        if (dconvers.switches.isLibrary()) {
            progressBar = new FackProgressBar();
        } else if (maxValue > Defaults.PROGRESS_SHOW_KILO_AFTER.getLongValue()) {
            progressBar = new CLIProgressBar(caption, maxValue, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, "K", 1000);
        } else {
            progressBar = new CLIProgressBar(caption, maxValue, Defaults.PROGRESS_UPDATE_INTERVAL_MILLISEC.getIntValue(), System.out, ProgressBarStyle.ASCII, " rows", 1);
        }
        progressBar.maxHint(maxValue);
        return progressBar;
    }

}
