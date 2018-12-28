package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.AppBase;
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

public class DataTable extends AppBase implements JRDataSource {

    private Object owner;

    private List<DataRow> dataRowList;
    private Map<String, DataRow> dataRowMap;
    private String idColumnName;
    private List<String> postUpdate;

    private DataTable metaData;
    private String dataSource;
    private String query;

    public DataTable(Application application, String tableName, String idColumnName) {
        super(application, tableName);

        this.idColumnName = idColumnName;
        this.owner = null;
        dataRowList = new ArrayList<>();
        dataRowMap = new HashMap<>();
        valid = true;
        dataSource = "";
        query = "";
        postUpdate = new ArrayList<>();
        currentRow = -1;
        needHeader = true;
    }

    public DataTable(Application application, String tableName, String idColumnName, List<String> postUpdate, Object owner) {
        super(application, tableName);

        this.postUpdate = postUpdate;
        this.idColumnName = idColumnName;
        this.owner = owner;
        dataRowList = new ArrayList<>();
        dataRowMap = new HashMap<>();
        valid = true;
        dataSource = "";
        query = "";
        currentRow = -1;
        needHeader = true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataTable.class);
    }

    public Object getOwner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public String getIdColumnName() {
        return idColumnName;
    }

    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }

    public DataTable getMetaData() {
        return metaData;
    }

    public void setMetaData(DataTable metaData) {
        this.metaData = metaData;
    }

    public List<DataRow> getRowList() {
        return dataRowList;
    }

    public void setRowList(List<DataRow> rowList) {
        this.dataRowList = rowList;

        dataRowMap.clear();
        if (rowList == null || rowList.size() == 0) {
            return;
        }

        for (DataRow row : dataRowList) {
            addRowToMap(row);
        }
    }

    public DataRow getRow(int row) {
        if (row >= dataRowList.size()) {
            return new DataRow(application, this);
        }
        return dataRowList.get(row);
    }

    public DataRow getRow(String idValue) {
        if (idValue == null) {
            return null;
        }
        return dataRowMap.get(idValue.toUpperCase());
    }

    public DataRow getRow(String sourceColumnName, String value) {
        if (sourceColumnName.equalsIgnoreCase(idColumnName)) {
            return getRow(value);
        }

        DataColumn dataColumn;
        for (DataRow dataRow : dataRowList) {
            dataColumn = dataRow.getColumn(sourceColumnName);
            if (dataColumn == null) {
                log.debug("DataTable({}).getRow({},{}). dataColumn({}) is null then return null", name, sourceColumnName, value, sourceColumnName);
                return null;
            }

            if (value.compareTo(dataColumn.getValue()) == 0) {
                return dataRow;
            }
        }

        log.debug("DataTable({}).getRow({},{}). return null", name, sourceColumnName, value);
        return null;
    }

    public void addRow(DataRow dataRow) {
        dataRowList.add(dataRow);
        addRowToMap(dataRow);
    }

    private void addRowToMap(DataRow dataRow) {
        String key;
        DataColumn idColumn = dataRow.getColumn(idColumnName);
        if (idColumn == null) {
            key = String.valueOf(dataRowList.size());
        } else {
            key = idColumn.getValue();
        }
        dataRowMap.put(key.toUpperCase(), dataRow);
    }

    public int getRowCount() {
        return dataRowList.size();
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
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
                .append("name", name)
                .append("idColumnName", idColumnName)
                .append("dataSource", dataSource)
                .append("query", query)
                .append("postUpdate", postUpdate)
                .append("metaData", metaData)
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
