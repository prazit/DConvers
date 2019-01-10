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

public class GetCalc extends Calc {

    private DataColumn value;

    public GetCalc(Application application, String name) {
        super(application, name);
    }

    @Override
    protected boolean prepare() {
        value = new DataString(application, 0, Types.VARCHAR, name, "");

        // get([current or [[TableType]:[TableName]]],[current or rowIndex],[columnIndex])
        String[] arguments = getArguments().split(",");
        if (arguments.length < 3) {
            error("CAL:GET need 3 arguments([current or [[TableType]:[TableName]]],[current or rowIndex],[columnIndex or columnName]). default value({}) is returned.", value);
            return false;
        }

        Converter converter = application.currentConverter;
        DataTable datatable = converter.getDataTable(arguments[0]);
        DataRow row = converter.getDataRow(arguments[1], datatable);
        DataColumn column = converter.getDataColumn(arguments[2], row);
        if (column == null) {
            error("CAL:GET. invalid column identifier, please check GET({})!, default value({}) is returned.", getArguments(), value);
            return false;
        }

        value = column.clone(0, name);

        return true;
    }

    @Override
    protected DataColumn calculate() {
        return value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(GetCalc.class);
    }
}
