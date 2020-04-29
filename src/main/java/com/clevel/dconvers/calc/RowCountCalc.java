package com.clevel.dconvers.calc;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataLong;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class RowCountCalc extends Calc {

    public RowCountCalc(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    private String defaultValue;
    private DataTable srcTable;

    @Override
    public boolean prepare() {
        defaultValue = "0";

        // rowcount([current or [[TableType]:[TableName]]])
        String[] arguments = getArguments().split(",");

        Converter converter = dconvers.currentConverter;
        srcTable = converter.getDataTable(arguments[0]);
        if (srcTable == null) {
            error("CAL:ROWCOUNT. invalid identifier, please check CAL:ROWCOUNT({})!, default value({}) is returned.", getArguments(), defaultValue);
            return false;
        }

        return true;
    }

    @Override
    protected DataColumn calculate() {
        return new DataLong(dconvers, 0, Types.INTEGER, name, (long) srcTable.getRowCount());
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(RowCountCalc.class);
    }

}
