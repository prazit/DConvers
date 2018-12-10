package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class CompileCalc extends Calc {

    private String value;

    public CompileCalc(Application application, String name) {
        super(application, name);
    }

    @Override
    protected boolean prepare() {
        value = "";

        // compile([current or [[TableType]:[TableName]]],[current or rowIndex],[columnIndex])
        String[] arguments = getArguments().split(",");
        if (arguments.length < 3) {
            error("CAL:COMPILE need 3 arguments([current or [[TableType]:[TableName]]],[current or rowIndex],[columnIndex]). default value(\"\") is returned.");
            return false;
        }

        DataTable datatable = getDataTable(arguments[0]);
        DataRow row = getDataRow(arguments[1], datatable);
        DataColumn column = getDataColumn(arguments[2], row);
        if (column == null) {
            error("CAL:COMPILE. invalid column identifier, please check GET({})!, default value(\"\") is returned.", getArguments());
            return false;
        }

        value = column.getValue();

        return true;
    }

    @Override
    protected DataColumn calculate() {
        String pureValue = application.currentConverter.compileDynamicValues(value);
        return new DataString(application, 0, Types.VARCHAR, name, pureValue);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(CompileCalc.class);
    }
}
