package com.clevel.dconvers.calc;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class NameCalc extends Calc {

    public NameCalc(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    private String defaultValue;
    private DataTable srcTable;

    @Override
    public boolean prepare() {
        defaultValue = "0";

        // name([current or [[TableType]:[TableName]]])
        String[] arguments = getArguments().split(",");

        Converter converter = dconvers.currentConverter;
        srcTable = converter.getDataTable(arguments[0]);
        if (srcTable == null) {
            error("CAL:NAME. invalid identifier, please check CAL:NAME({})!, default value({}) is returned.", getArguments(), defaultValue);
            return false;
        }

        return true;
    }

    @Override
    protected DataColumn calculate() {
        return new DataString(dconvers, 0, Types.VARCHAR, name, srcTable.getName());
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(NameCalc.class);
    }

}
