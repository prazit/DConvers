package com.clevel.dconvers.calc;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.List;

public class ConcatCalc extends Calc {

    public ConcatCalc(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    private Converter converter;
    private DataColumn defaultValue;
    private String rowIndex;
    private DataTable srcTable;
    private List<Integer> columnIndexList;

    @Override
    public boolean prepare() {
        defaultValue = new DataString(dconvers, 0, Types.VARCHAR, "default", "");

        // concat([current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..)
        String[] arguments = getArguments().split(",");

        converter = dconvers.currentConverter;
        srcTable = converter.getDataTable(arguments[0]);
        if (srcTable == null) {
            error("CAL:CONCAT. invalid identifier, please check CAL:CONCAT({})!, default value(\"{}\") is returned.", defaultValue);
            return false;
        }

        rowIndex = arguments[1];
        DataRow firstRow = srcTable.getRow(0);
        columnIndexList = createIndexList(firstRow, arguments, 2);

        return true;
    }

    @Override
    protected DataColumn calculate() {

        DataRow currentRow = converter.getDataRow(rowIndex, srcTable);
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

        return new DataString(dconvers, 0, Types.VARCHAR, name, value);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConcatCalc.class);
    }

}
