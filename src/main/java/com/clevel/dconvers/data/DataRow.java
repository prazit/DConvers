package com.clevel.dconvers.data;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.ngin.AppBase;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataRow extends AppBase {

    private DataTable dataTable;
    private HashMap<String, DataColumn> dataColumnMap;
    private List<DataColumn> columnList;

    public DataRow(DConvers dconvers, DataTable dataTable) {
        super(dconvers, dataTable.getName());

        this.dataTable = dataTable;
        dataColumnMap = new HashMap<>();
        columnList = new ArrayList<>();
        valid = dataTable != null;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataRow.class);
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public int getColumnIndex(String columnName) {
        DataColumn dataColumn = getColumn(columnName);
        if (dataColumn == null) {
            return -2;
        }

        return columnList.indexOf(dataColumn);
    }

    public DataColumn getColumn(String columnName) {
        if (columnName == null) {
            return null;
        }

        DataColumn dataColumn = dataColumnMap.get(columnName.toUpperCase());
        return dataColumn;
    }

    public DataColumn getColumn(int columnIndex) {
        try {
            return columnList.get(columnIndex);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public HashMap<String, DataColumn> getColumnMap() {
        return dataColumnMap;
    }

    public List<DataColumn> getColumnList() {
        return columnList;
    }

    public int getColumnCount() {
        return columnList.size();
    }

    public void putColumn(String columnName, DataColumn dataColumn) {
        columnName = columnName.toUpperCase();
        DataColumn existing = dataColumnMap.get(columnName);
        try {
            if (existing != null) {
                int index = columnList.indexOf(existing);
                columnList.remove(index);
                columnList.add(index, dataColumn);
            } else {
                columnList.add(dataColumn);
            }
            dataColumnMap.put(columnName, dataColumn);
        } catch (Exception e) {
            dataColumnMap.put(columnName, new DataString(dconvers, 0, 0, null, null));
        }
    }

    /**
     * When you get column list and modify content within them, you need to call this function to update the column map to make sure all processes will work correctly.
     */
    public void updateColumnMap() {
        dataColumnMap = new HashMap<>();

        for (DataColumn column : columnList) {
            dataColumnMap.put(column.getName().toUpperCase(), column);
        }
    }

    public DataRow clone() {
        DataRow newRow = new DataRow(dconvers, dataTable);

        DataColumn newColumn;
        String columnName;
        for (DataColumn dataColumn : columnList) {
            columnName = dataColumn.getName();
            newColumn = dataColumn.clone(dataColumn.getIndex(), columnName);
            newRow.putColumn(newColumn.getName(), newColumn);
        }

        return newRow;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("valid", valid)
                .append("Columns", dataColumnMap)
                .toString()
                .replace('=', ':');
    }

}