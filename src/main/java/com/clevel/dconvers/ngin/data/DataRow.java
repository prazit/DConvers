package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.ngin.ValidatorBase;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataRow extends ValidatorBase {

    private DataTable dataTable;
    private Map<String, DataColumn> dataColumnMap;
    private List<DataColumn> columnList;

    public DataRow(DataTable dataTable) {
        this.dataTable = dataTable;
        dataColumnMap = new HashMap<>();
        columnList = new ArrayList<>();
        valid = dataTable != null;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public DataColumn getColumn(String columnName) {
        if (columnName == null) {
            return null;
        }

        DataColumn dataColumn = dataColumnMap.get(columnName);
        return dataColumn;
    }

    public Map<String, DataColumn> getColumnMap() {
        return dataColumnMap;
    }

    public List<DataColumn> getColumnList() {
        return columnList;
    }

    public int getColumnCount() {
        return columnList.size();
    }

    public void putColumn(String columnName, DataColumn dataColumn) {
        try {
            columnList.add(dataColumn);
            dataColumnMap.put(columnName, dataColumn);
        } catch (Exception e) {
            dataColumnMap.put(columnName, new DataString(0, 0, null, null));
        }
    }

    /**
     * When you get column list and modify content within them, you need to call this function to update the column map to make sure all processes will work correctly.
     */
    public void updateColumnMap() {
        dataColumnMap = new HashMap<>();

        for (DataColumn column : columnList) {
            dataColumnMap.put(column.getName(), column);
        }
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