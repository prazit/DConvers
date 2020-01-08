package com.clevel.dconvers.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class GetCalc extends Calc {

    protected DataColumn value;

    private boolean useDynamicIdentifier;
    private String rowIdentifier;
    private String columnIdentifier;
    private Converter converter;
    private DataTable datatable;

    public GetCalc(Application application, String name) {
        super(application, name);
        useDynamicIdentifier = false;
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

        converter = application.currentConverter;
        datatable = converter.getDataTable(arguments[0]);
        rowIdentifier = arguments[1];
        columnIdentifier = arguments[2];

        // Has dynamic value in Row/Column Identifier.
        if (arguments[1].contains(":")) {
            useDynamicIdentifier = true;
            return true;
        }

        return prepareValue();
    }

    protected boolean prepareValue() {
        DataRow row = converter.getDataRow(rowIdentifier, datatable);
        DataColumn column = converter.getDataColumn(columnIdentifier, row);
        if (column == null) {
            error("CAL:GET. invalid column identifier({}) in table({}) please check expression({}), default value({}) is returned.", columnIdentifier, datatable.getTableType() + ":" + datatable.getName(), getArguments(), value);
            return false;
        }

        value = column.clone(0, name);
        return true;
    }

    @Override
    protected DataColumn calculate() {
        if (useDynamicIdentifier) {
            prepareValue();
        }
        return value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(GetCalc.class);
    }
}
