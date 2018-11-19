package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConcatCalc extends Calc {

    public ConcatCalc(Application application, String name) {
        super(application, name);
    }

    private String defaultValue;
    private String rowIndex;
    private DataTable srcTable;
    private List<Integer> columnIndexList;

    @Override
    public boolean prepare() {
        defaultValue = "";

        // concat([current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..)
        String[] arguments = getArguments().split(",");

        srcTable = getDataTable(arguments[0]);
        if (srcTable == null) {
            log.debug("ConcatCalculator.srcTable is null!, default value(\"{}\") is returned.", defaultValue);
            return false;
        }

        rowIndex = arguments[1];
        DataRow firstRow = srcTable.getRow(0);
        int maxIndex = firstRow.getColumnCount() - 1;
        columnIndexList = createIndexList(arguments, 2, 0, maxIndex);

        return true;
    }

    @Override
    protected String calculate() {

        DataRow currentRow = getDataRow(rowIndex, srcTable);
        if (currentRow == null) {
            log.debug("ConcatCalculator.currentRow is null!, default value(\"{}\") is returned.", defaultValue);
            return defaultValue;
        }

        List<DataColumn> columnList = currentRow.getColumnList();
        DataColumn column;
        String value = "";
        for (Integer index : columnIndexList) {
            column = columnList.get(index);
            value += column.getValue();
        }

        return value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConcatCalc.class);
    }

}
