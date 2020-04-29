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

public class CompileCalc extends Calc {

    private String value;

    public CompileCalc(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    protected boolean prepare() {
        value = "";

        // compile([current or [[TableType]:[TableName]]],[current or rowIndex],[columnIndex or columnName])
        String[] arguments = getArguments().split(",");
        if (arguments.length < 3) {
            error("CAL:COMPILE need 3 arguments([current or [[TableType]:[TableName]]],[current or rowIndex],[columnIndex]). default value(\"\") is returned.");
            return false;
        }

        Converter converter = dconvers.currentConverter;
        DataTable datatable = converter.getDataTable(arguments[0]);
        DataRow row = converter.getDataRow(arguments[1], datatable);
        DataColumn column = converter.getDataColumn(arguments[2], row);
        if (column == null) {
            error("CAL:COMPILE. invalid column identifier, please check GET({})!, default value(\"\") is returned.", getArguments());
            return false;
        }

        value = column.getValue();

        return true;
    }

    @Override
    protected DataColumn calculate() {
        String pureValue = dconvers.currentConverter.compileDynamicValues(value);
        return new DataString(dconvers, 0, Types.VARCHAR, name, pureValue);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(CompileCalc.class);
    }
}
