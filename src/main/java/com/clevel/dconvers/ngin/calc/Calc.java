package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.UtilBase;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;

public abstract class Calc extends UtilBase {

    private String arguments;

    private Converter currentConverter;
    private DataTable currentTable;
    private int currentRowIndex;

    private boolean prepared;

    public void setArguments(String arguments) {
        this.arguments = arguments;
        prepared = false;
    }

    public void setCurrentConverter(Converter currentConverter) {
        this.currentConverter = currentConverter;
        prepared = false;
    }

    public void setCurrentTable(DataTable currentTable) {
        this.currentTable = currentTable;
        prepared = false;
    }

    public void setCurrentRowIndex(int currentRowIndex) {
        this.currentRowIndex = currentRowIndex;
    }

    protected String getArguments() {
        return arguments;
    }

    protected Converter getCurrentConverter() {
        return currentConverter;
    }

    protected DataTable getCurrentTable() {
        return currentTable;
    }

    protected int getCurrentRowIndex() {
        return currentRowIndex;
    }

    public Calc(Application application, String name) {
        super(application, name);

        currentRowIndex = -1;
        currentTable = null;
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
        if (Property.CURRENT.key().equalsIgnoreCase(tableIdentifier)) {
            return currentTable;
        }

        if (currentConverter == null) {
            return null;
        }

        return currentConverter.getDataTable(tableIdentifier);
    }

    protected DataRow getDataRow(String rowIdentifier, DataTable dataTable) {
        if (dataTable == null) {
            return null;
        }

        if (Property.CURRENT.key().equalsIgnoreCase(rowIdentifier)) {
            DataRow row = dataTable.getRow(currentRowIndex);
            if (row.getColumnCount() == 0) {
                return null;
            }
            return row;
        }

        DataRow row = dataTable.getRow(Integer.parseInt(rowIdentifier));
        if (row.getColumnCount() == 0) {
            return null;
        }

        return row;
    }
}
