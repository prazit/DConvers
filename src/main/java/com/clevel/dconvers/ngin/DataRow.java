package com.clevel.dconvers.ngin;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;

public class DataRow extends ValidatorBase {

    private DataTable dataTable;
    private Map<String, DataColumn> dataColumnMap;

    DataRow(DataTable dataTable) {
        this.dataTable = dataTable;
        dataColumnMap = new HashMap<>();
        valid = dataTable != null;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public DataColumn getColumn(String columnName) {
        DataColumn dataColumn;
        try {
            dataColumn = dataColumnMap.get(columnName);
        } catch (Exception e) {
            dataColumn = null;
        }
        return dataColumn;
    }

    void putColumn(String columnName, DataColumn dataColumn) {
        try {
            dataColumnMap.put(columnName, dataColumn);
        } catch (Exception e) {
            dataColumnMap.put(columnName, new DataString(0, 0, null, null));
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