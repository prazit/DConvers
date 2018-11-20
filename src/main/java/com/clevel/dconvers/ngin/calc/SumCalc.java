package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;

public class SumCalc extends Calc {

    public SumCalc(Application application, String name) {
        super(application, name);
    }

    private String rowIndex;
    private DataTable srcTable;
    private String defaultValue;
    private List<Integer> columnIndexList;

    @Override
    protected boolean prepare() {
        defaultValue = "0";

        // sum([replace or [ColumnName]]:[insertColumnIndex],[current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..)
        String[] arguments = getArguments().split(",");
        int argumentCount = arguments.length;
        if (argumentCount < 3) {
            return false;
        }

        srcTable = getDataTable(arguments[0]);
        if (srcTable == null || srcTable.getRowCount() == 0) {
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
            return defaultValue;
        }

        List<DataColumn> columnList = currentRow.getColumnList();
        DataColumn column;
        Long longValue;
        BigDecimal bigDecimalValue;
        double sum = 0.0;
        for (Integer index : columnIndexList) {
            column = columnList.get(index);
            switch (column.getType()) {
                case Types.BIGINT:
                case Types.INTEGER:
                    longValue = ((DataLong) column).getLongValue();
                    sum += longValue == null ? 0.0 : longValue.doubleValue();
                    break;

                case Types.DECIMAL:
                    bigDecimalValue = ((DataBigDecimal) column).getBigDecimalValue();
                    sum += bigDecimalValue == null ? 0.0 : bigDecimalValue.doubleValue();
                    break;

                default:
            }
        }

        String value = String.valueOf(sum);
        if (value.endsWith(".0")) {
            value = value.substring(0, value.length() - 2);
        }
        return value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SumCalc.class);
    }
}