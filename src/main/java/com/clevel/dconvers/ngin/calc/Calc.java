package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.UtilBase;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
import com.clevel.dconvers.ngin.data.DataTable;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.Types;

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

    protected abstract DataColumn calculate();

    public DataColumn calc() {
        if (!prepared) {
            if (!prepare()) {
                return new DataString(application, 0, Types.VARCHAR, "prepare_is_failed", "");
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

        DataColumn dataColumn;
        if (NumberUtils.isCreatable(columnIndex)) {
            dataColumn = dataRow.getColumn(NumberUtils.createInteger(columnIndex) - 1);
        } else {
            dataColumn = dataRow.getColumn(columnIndex);
        }
        
        return dataColumn;
    }
}
