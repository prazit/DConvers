package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.List;

public class ConcatCalc extends Calc {

    public ConcatCalc(Application application, String name) {
        super(application, name);
    }

    private Converter converter;
    private DataColumn defaultValue;
    private String rowIndex;
    private DataTable srcTable;
    private List<Integer> columnIndexList;

    @Override
    public boolean prepare() {
        defaultValue = new DataString(application, 0, Types.VARCHAR, "default", "");

        // concat([current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..)
        String[] arguments = getArguments().split(",");

        converter = application.currentConverter;
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

        return new DataString(application, 0, Types.VARCHAR, name, value);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConcatCalc.class);
    }

}
