package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
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

        // value([replace or [ColumnNameToInsert]]:[ColumnIndexToInsert],[current or [[TableType]:[TableName]]],[[ColumnRange] or [ColumnIndex]],..)
        String argument = getArgument(Property.ARGUMENTS.key());
        String[] arguments = argument.split("[,]");
        String[] firstArg = arguments[0].split("[:]");
        String newColumnName = firstArg[0];
        int newColumnIndex = Integer.parseInt(firstArg[1]) - 1;

        List<DataRow> newRowList = new ArrayList<>();
        List<DataRow> rowList = dataTable.getAllRow();

        List<Integer> indexList = createIndexList(arguments, 2, 0, rowList.get(0).getColumnList().size() - 1);

        List<DataColumn> columnList;
        DataColumn column;
        String value = "";
        DataRow newRow;

        for (DataRow row : rowList) {

            columnList = row.getColumnList();
            for (Integer index : indexList) {
                column = columnList.get(index);
                value += column.getValue();
            }

            newRow = insertReplaceColumn(row, newColumnName, newColumnIndex, new DataString(0, Types.VARCHAR, newColumnName, value));
            if (newRow == null) {
                return false;
            }
            newRowList.add(newRow);

        }

        rowList.clear();
        rowList.addAll(newRowList);

        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConcatTransform.class);
    }

}
