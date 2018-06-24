package com.clevel.dconvers.ngin;

import java.util.HashMap;
import java.util.Map;

public class DataRow extends ValidatorBase {

    private DataTable dataTable;
    private Map<String, DataColumn> dataColumnMap;

    DataRow(DataTable dataTable) {
        this.dataTable = dataTable;
        dataColumnMap = new HashMap<>();
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public DataColumn getColumn(String columnName) {
        DataColumn dataColumn;
        try {
            dataColumn = dataColumnMap.get(columnName);
        } catch (Exception e) {
            dataColumn = new DataColumn() {
                @Override
                public String getValue() {
                    return "";
                }
            };
        }
        return dataColumn;
    }

    void putColumn(String columnName, DataColumn dataColumn) {
        try {
            dataColumnMap.put(columnName, dataColumn);
        } catch (Exception e) {
            dataColumnMap.put(columnName, new DataColumn() {
                @Override
                public String getValue() {
                    return "";
                }
            });
        }
    }

}