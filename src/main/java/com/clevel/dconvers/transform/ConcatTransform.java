package com.clevel.dconvers.transform;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ConcatTransform extends Transform {

    public ConcatTransform(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        // CONCAT([replace or [ColumnNameToInsert]]:[ColumnIndexToInsert],[[ColumnRange] or [ColumnIndex]],..)
        String argument = getArgument(Property.ARGUMENTS.key());
        String[] arguments = argument.split("[,]");
        List<DataRow> rowList = dataTable.getRowList();
        List<DataRow> newRowList = new ArrayList<>();
        List<Integer> indexList = createIndexList(rowList.get(0), arguments, 1);

        long rowCount = rowList.size();
        String tableName = dataTable.getName();

        String[] firstArg = arguments[0].split("[:]");
        String newColumnName = firstArg[0];
        List<Integer> newColumnIndex = createIndexList(rowList.get(0), firstArg[1]);
        if (newColumnIndex.size() == 0) {
            error("Invalid columIdentifier({}) for CONCAT transformation of table({})", dataTable.getName());
            return false;
        }

        List<DataColumn> columnList;
        DataColumn column;
        String value = "";
        DataRow newRow;

        ProgressBar progressBar = getProgressBar("Transform table(" + tableName + ") by CONCAT", rowCount);
        for (DataRow row : rowList) {
            columnList = row.getColumnList();

            value = "";
            for (Integer index : indexList) {
                column = columnList.get(index);
                value += column.getValue();
            }

            newRow = insertReplaceColumn(row, newColumnName, newColumnIndex.get(0), new DataString(dconvers, 0, Types.VARCHAR, newColumnName, value));
            if (newRow == null) {
                progressBar.close();
                return false;
            }
            newRowList.add(newRow);

            progressBar.step();
        }
        progressBar.close();

        rowList.clear();
        rowList.addAll(newRowList);

        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConcatTransform.class);
    }

}
