package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class FormatCalc extends Calc {

    public FormatCalc(Application application, String name) {
        super(application, name);
    }

    private String value;

    @Override
    public boolean prepare() {
        value = "";

        // format([current or [[TableType]:[TableName]]],[current or rowIndex],[columnIndex],[formatPattern])
        String[] arguments = getArguments().split(",");
        if (arguments.length < 4) {
            error("CAL:FORMAT need 4 arguments!, default value(\"{}\") is used.", value);
            return false;
        }

        DataTable datatable = getDataTable(arguments[0]);
        DataRow row = getDataRow(arguments[1], datatable);
        DataColumn column = getDataColumn(arguments[2], row);
        if (column == null) {
            error("CAL:FORMAT. invalid value identifiers, please check FORMAT({})!, default value(\"{}\") is used.", getArguments(), value);
            return false;
        }

        value = column.getFormattedValue(arguments[3]);

        return true;
    }

    @Override
    protected DataColumn calculate() {
        return new DataString(application, 0, Types.VARCHAR, name, value);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(FormatCalc.class);
    }

}
