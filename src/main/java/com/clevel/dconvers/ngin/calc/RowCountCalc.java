package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowCountCalc extends Calc {

    public RowCountCalc(Application application, String name) {
        super(application, name);
    }

    private String defaultValue;
    private DataTable srcTable;

    @Override
    public boolean prepare() {
        defaultValue = "0";

        // rowcount([current or [[TableType]:[TableName]]])
        String[] arguments = getArguments().split(",");

        srcTable = getDataTable(arguments[0]);
        if (srcTable == null) {
            log.debug("RowCountCalculator.srcTable is null!, default value({}) is returned.", defaultValue);
            return false;
        }

        return true;
    }

    @Override
    protected String calculate() {
        return String.valueOf(srcTable.getRowCount());
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(RowCountCalc.class);
    }

}
