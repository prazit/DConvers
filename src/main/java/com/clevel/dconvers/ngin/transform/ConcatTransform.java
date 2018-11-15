package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ConcatTransform extends Transform {

    public ConcatTransform(Application application, String name) {
        super(application, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        String argument = getArgument(Property.ARGUMENTS.key());
        String[] arguments = argument.split("[,]");
        String[] firstArg = arguments[0].split("[:]");
        String newColumnName = firstArg[0];
        int newColumnIndex = Integer.parseInt(firstArg[1]) - 1;

        List<DataRow> newRowList = new ArrayList<>();
        List<DataRow> rowList = dataTable.getAllRow();
        DataRow newRow;

        List<Integer> indexList = createIndexList(arguments, 1, 1, rowList.get(0).getColumnList().size());

        List<DataColumn> newColumnList;
        DataColumn newColumn;

        for (DataRow row : rowList) {
            newColumn = application.createDataColumn(newColumnName, Types.VARCHAR, "");

            for (Integer index : indexList) {
                if (!concatByIndex(newColumn, row, index)) {
                    return false;
                }
            }

            newRow = new DataRow(dataTable);
            newRowList.add(newRow);
            newColumnList = newRow.getColumnList();
            newColumnList.addAll(row.getColumnList());
            newColumnList.add(newColumnIndex, newColumn);
            newRow.updateColumnMap();
        }

        rowList.clear();
        rowList.addAll(newRowList);

        return true;

    }

    private boolean concatByIndex(DataColumn column, DataRow row, int index) {

        String value = column.getValue();

        DataColumn dataColumn = row.getColumnList().get(index);
        String concat = dataColumn.getValue();

        value += concat;
        column.setValue(value);

        return true;

    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConcatTransform.class);
    }

}
