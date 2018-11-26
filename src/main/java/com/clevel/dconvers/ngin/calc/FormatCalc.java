package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            log.error("FormatValueCalculator. 4 arguments are required!, default value({}) is used.", value);
            return false;
        }

        DataTable datatable = getDataTable(arguments[0]);
        DataRow row = getDataRow(arguments[1], datatable);
        DataColumn column = getDataColumn(arguments[2],row);
        if (column == null) {
            log.debug("FormatValueCalculator. invalid value identifiers, please check the value identifiers!, default value({}) is used.", arguments[2], value);
            return false;
        }

        value = column.getFormattedValue(arguments[3]);

        return true;
    }

    @Override
    protected String calculate() {
        return value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(FormatCalc.class);
    }

}
