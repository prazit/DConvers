package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.UtilBase;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;

public abstract class Calc extends UtilBase {

    private String arguments;
    private boolean prepared;

    public void setArguments(String arguments) {
        this.arguments = arguments;
        prepared = false;
    }

    protected String getArguments() {
        return arguments;
    }

    public Calc(Application application, String name) {
        super(application, name);

        prepared = false;
    }

    protected abstract boolean prepare();

    protected abstract String calculate();

    public String calc() {
        if (!prepared) {
            if (!prepare()) {
                return "";
            }
            prepared = true;
        }

        return calculate();
    }

    protected DataTable getDataTable(String tableIdentifier) {
        Converter currentConverter = application.currentConverter;

        if (Property.CURRENT.key().equalsIgnoreCase(tableIdentifier)) {
            return currentConverter.getCurrentTable();
        }

        return currentConverter.getDataTable(tableIdentifier);
    }

    protected DataRow getDataRow(String rowIdentifier, DataTable dataTable) {
        if (dataTable == null) {
            return null;
        }

        Converter currentConverter = application.currentConverter;
        if (Property.CURRENT.key().equalsIgnoreCase(rowIdentifier)) {
            DataRow row = dataTable.getRow(currentConverter.getCurrentRowIndex());
            if (row.getColumnCount() == 0) {
                return null;
            }
            return row;
        }

        DataRow row = dataTable.getRow(Integer.parseInt(rowIdentifier) - 1);
        if (row.getColumnCount() == 0) {
            return null;
        }

        return row;
    }

    protected DataColumn getDataColumn(String columnIndex, DataRow dataRow) {
        if (dataRow == null) {
            return null;
        }

        return dataRow.getColumn(Integer.parseInt(columnIndex) - 1);
    }
}
