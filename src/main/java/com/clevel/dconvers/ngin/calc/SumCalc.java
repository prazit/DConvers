package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
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

    private Converter converter;
    private String rowIdentifier;
    private DataTable srcTable;
    private DataColumn defaultValue;
    private List<Integer> columnIndexList;

    @Override
    protected boolean prepare() {
        defaultValue = new DataLong(application, 0, Types.INTEGER, "default", 0L);

        // sum([current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..)
        String[] arguments = getArguments().split(",");
        int argumentCount = arguments.length;
        if (argumentCount < 3) {
            error("CAL:SUM need 3 arguments([current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..). default value({}) is returned.", defaultValue);
            return false;
        }

        converter = application.currentConverter;
        srcTable = converter.getDataTable(arguments[0]);
        if (srcTable == null || srcTable.getRowCount() == 0) {
            error("CAL:CONCAT. invalid identifier, please check CAL:SUM({})!, default value(\"{}\") is returned.", getArguments(), defaultValue);
            return false;
        }

        rowIdentifier = arguments[1];

        DataRow firstRow = srcTable.getRow(0);
        columnIndexList = createIndexList(firstRow, arguments, 2);

        return true;
    }

    @Override
    protected DataColumn calculate() {
        DataRow currentRow = converter.getDataRow(rowIdentifier, srcTable);
        if (currentRow == null) {
            return defaultValue;
        }
        log.debug("SumCalc.calculate() datatable({}) datarow({})", srcTable.getName(), currentRow.toString());

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
                    try {
                        bigDecimalValue = new BigDecimal(column.getValue());
                    } catch (Exception ex) {
                        bigDecimalValue = null;
                    }
                    sum += bigDecimalValue == null ? 0.0 : bigDecimalValue.doubleValue();
            }
            log.debug("at colum(index:{},name:{},value:{}) sum = {}", index, column.getName(), column.getValue(), sum);
        }

        String value = String.valueOf(sum);
        bigDecimalValue = new BigDecimal(sum);
        if (value.endsWith(".0")) {
            //value = value.substring(0, value.length() - 2);
            return new DataBigDecimal(application, 0, Types.DECIMAL, name, bigDecimalValue);
        } else {
            return new DataLong(application, 0, Types.INTEGER, name, bigDecimalValue.longValue());
        }

    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SumCalc.class);
    }
}
