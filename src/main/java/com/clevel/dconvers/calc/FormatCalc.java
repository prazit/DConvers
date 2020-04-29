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

public class FormatCalc extends Calc {

    public FormatCalc(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    private String value;

    @Override
    public boolean prepare() {
        value = "";

        // format([current or [[TableType]:[TableName]]],[current or rowIndex],[columnIndex or columnName],[formatPattern])
        String[] arguments = getArguments().split(",");
        if (arguments.length < 4) {
            error("CAL:FORMAT need 4 arguments!, default value(\"{}\") is used.", value);
            return false;
        }

        Converter converter = dconvers.currentConverter;
        DataTable datatable = converter.getDataTable(arguments[0]);
        DataRow row = converter.getDataRow(arguments[1], datatable);
        DataColumn column = converter.getDataColumn(arguments[2], row);
        if (column == null) {
            error("CAL:FORMAT. invalid value identifiers, please check FORMAT({})!, default value(\"{}\") is used.", getArguments(), value);
            return false;
        }

        value = column.getFormattedValue(arguments[3]);

        return true;
    }

    @Override
    protected DataColumn calculate() {
        return new DataString(dconvers, 0, Types.VARCHAR, name, value);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(FormatCalc.class);
    }

}
