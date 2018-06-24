package com.clevel.dconvers.ngin;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class DataTable extends ValidatorBase {

    private List<DataRow> dataRowList;
    private ResultSetMetaData metaData;
    private String tableName;

    DataTable(String tableName) {
        this.tableName = tableName;
        dataRowList = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public ResultSetMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(ResultSetMetaData metaData) {
        this.metaData = metaData;
    }

    public List<DataRow> getAllRow() {
        return dataRowList;
    }

    public DataRow getRow(int row) {
        if (row > dataRowList.size()) {
            return new DataRow(this);
        }
        return dataRowList.get(row);
    }

    public void addRow(DataRow dataRow) {
        dataRowList.add(dataRow);
    }

    public int getRowCount() {
        return dataRowList.size();
    }

    @Override
    public String toString() {

        /*System.out.println("-- Meta Data --");
        int cols = metaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            System.out.println("column( " + String.valueOf(i) + ", " + metaData.getColumnName(i) + " = " + metaData.getColumnTypeName(i) + " )");
        }*/

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(super.toString())
                .append("valid", valid)
                .append("tableName", tableName)
                .append("metaData", metaData)
                .append("dataRowList", dataRowList)
                .toString();
    }
}
