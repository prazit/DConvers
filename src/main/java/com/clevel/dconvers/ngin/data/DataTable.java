package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.ngin.ValidatorBase;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTable extends ValidatorBase implements JRDataSource {

    private List<DataRow> dataRowList;
    private Map<String, DataRow> dataRowMap;
    private ResultSetMetaData metaData;
    private String tableName;
    private String idColumnName;
    private String query;
    private List<String> postUpdate;

    private Logger log;

    public DataTable(String tableName, String idColumnName) {
        this.tableName = tableName;
        this.idColumnName = idColumnName;
        dataRowList = new ArrayList<>();
        dataRowMap = new HashMap<>();
        valid = true;
        query = "";
        postUpdate = new ArrayList<>();
        currentRow = -1;
        needHeader = true;
        log = LoggerFactory.getLogger(DataTable.class);
    }

    public DataTable(String tableName, String idColumnName, List<String> postUpdate) {
        this.tableName = tableName;
        this.postUpdate = postUpdate;
        this.idColumnName = idColumnName;
        dataRowList = new ArrayList<>();
        dataRowMap = new HashMap<>();
        valid = true;
        query = "";
        currentRow = -1;
        needHeader = true;
        log = LoggerFactory.getLogger(DataTable.class);
    }

    public String getTableName() {
        return tableName;
    }

    public String getIdColumnName() {
        return idColumnName;
    }

    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
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
        if (row >= dataRowList.size()) {
            return new DataRow(this);
        }
        return dataRowList.get(row);
    }

    public DataRow getRow(String idValue) {
        if (idValue == null) {
            return null;
        }
        return dataRowMap.get(idValue);
    }

    public DataRow getRow(String sourceColumnName, String value) {
        DataColumn dataColumn;

        Logger log = LoggerFactory.getLogger(DataTable.class);

        for (DataRow dataRow : dataRowList) {
            dataColumn = dataRow.getColumn(sourceColumnName);
            if (dataColumn == null) {
                log.debug("DataTable({}).getRow({},{}). dataColumn({}) is null then return null", tableName, sourceColumnName, value, sourceColumnName);
                return null;
            }

            if (value.compareTo(dataColumn.getValue()) == 0) {
                return dataRow;
            }
        }

        log.debug("DataTable({}).getRow({},{}). return null", tableName, sourceColumnName, value);
        return null;
    }

    public void addRow(DataRow dataRow) {
        dataRowList.add(dataRow);

        String key;
        DataColumn idColumn = dataRow.getColumn(idColumnName);
        if (idColumn == null) {
            key = String.valueOf(dataRowList.size());
        } else {
            key = idColumn.getValue();
        }
        dataRowMap.put(key, dataRow);
    }

    public int getRowCount() {
        return dataRowList.size();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getPostUpdate() {
        return postUpdate;
    }

    public void setPostUpdate(List<String> postUpdate) {
        this.postUpdate = postUpdate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("dataRowList", dataRowList)
                .append("dataRowMap", dataRowMap)
                .append("metaData", metaData)
                .append("tableName", tableName)
                .append("idColumnName", idColumnName)
                .append("query", query)
                .append("postUpdate", postUpdate)
                .toString()
                .replace('=', ':');
    }

    private boolean needHeader;
    private int currentRow;
    private List<DataColumn> columnList;

    @Override
    public boolean next() throws JRException {
        int rowCount = getRowCount();
        //log.debug("next of currentRow({}), rowcount({})", currentRow, rowCount);
        if (rowCount == 0) {
            return false;
        }

        if (needHeader) {
            columnList = getRow(0).getColumnList();
            if (currentRow < 0) {
                currentRow++;
            } else {
                needHeader = false;
            }
            return true;
        }

        if (currentRow >= rowCount) {
            columnList = getRow(0).getColumnList();
            return false;
        }

        currentRow++;
        columnList = getRow(currentRow).getColumnList();
        return true;
    }

    /**
     * @param jrField name of field in jrxml file (row=0 is column headers) (row>0 is column values)
     * @return
     * @throws JRException
     */
    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        // field1,field2,...
        String fieldName = jrField.getName();
        int columnIndex = Integer.parseInt(fieldName.substring(5)) - 1;
        //log.debug("getFieldValue(name:{}, desc:{}, class:{}) columnIndex is {}", fieldName, jrField.getDescription(), jrField.getValueClassName(), columnIndex);

        if (columnIndex >= columnList.size()) {
            return "-";
        }

        DataColumn dataColumn = columnList.get(columnIndex);
        if (needHeader) {
            return dataColumn.getName();
        }
        return dataColumn.getValue();
    }

}
