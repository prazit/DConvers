package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.ngin.ValidatorBase;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class DataTable extends ValidatorBase {

    private List<DataRow> dataRowList;
    private ResultSetMetaData metaData;
    private String tableName;

    public DataTable(String tableName) {
        this.tableName = tableName;
        dataRowList = new ArrayList<>();
        valid = true;
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
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("valid", valid)
                .append("tableName", tableName)
                .append("dataRowList", dataRowList)
                .toString();
    }
}
