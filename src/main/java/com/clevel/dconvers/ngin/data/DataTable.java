package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.ngin.AppBase;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.Target;
import com.clevel.dconvers.ngin.dynvalue.DynamicValueType;
import com.clevel.dconvers.ngin.output.OutputFactory;
import com.clevel.dconvers.ngin.output.OutputTypes;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataTable extends AppBase implements JRDataSource {

    protected Object owner;

    /* dataRowMap is HashMap<ColumnName,HashMap<ValueString,DataRow>>
     * ValueString is DataColumn.getValue()
     **/
    protected HashMap<String, HashMap<String, DataRow>> dataRowMap;
    protected List<DataRow> dataRowList;
    protected String idColumnName;

    protected List<String> postUpdate;

    protected DataTable metaData;
    protected String dataSource;
    protected String query;

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
        dataRowMap.put(idColumnName, createMap(idColumnName));
    }

    private HashMap<String, DataRow> createMap(String columnName) {
        HashMap<String, DataRow> hashMap = new HashMap<>();
        if (dataRowList.size() == 0) {
            return hashMap;
        }

        String key;
        DataColumn dataColumn;
        DataRow dataRow = dataRowList.get(0);
        dataColumn = dataRow.getColumn(columnName);
        if (dataColumn == null) {
            error("DataTable({}).createMap. column({}) not found!", name, columnName);
            return hashMap;
        }

        for (DataRow row : dataRowList) {
            dataColumn = row.getColumn(columnName);
            key = dataColumn.getValue();
            if (key == null) {
                key = "NULL";
            }
            hashMap.put(key.toUpperCase(), row);
        }

        return hashMap;
    }

    public DataRow getRow(int row) {
        if (row >= dataRowList.size()) {
            return new DataRow(application, this);
        }
        return dataRowList.get(row);
    }

    public DataRow getRow(String idValue) {
        if (idValue == null) {
            idValue = "NULL";
        }else{
            idValue = idValue.toUpperCase();
        }
        return getHashMap(idColumnName.toUpperCase()).get(idValue);
    }

    private HashMap<String, DataRow> getHashMap(String columnName) {
        HashMap<String, DataRow> hashMap = dataRowMap.get(columnName.toUpperCase());
        if (hashMap == null) {
            hashMap = createMap(columnName);
            dataRowMap.put(columnName.toUpperCase(), hashMap);
        }
        return hashMap;
    }

    public DataRow getRow(String sourceColumnName, String value) {
        if (value == null) {
            value = "NULL";
        }else{
            value = value.toUpperCase();
        }
        return getHashMap(sourceColumnName.toUpperCase()).get(value);
    }

    public void addRow(DataRow dataRow) {
        dataRowList.add(dataRow);

        HashMap<String, DataRow> hashMap = getHashMap(idColumnName);

        String key;
        DataColumn dataColumn = dataRow.getColumn(idColumnName);
        if (dataColumn == null) {
            key = "NULL";
        }else {
            key = dataColumn.getValue();
        }

        hashMap.put(key.toUpperCase(), dataRow);
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

    public DynamicValueType getTableType() {
        if (!isValid() || owner == null) {
            return DynamicValueType.INV;
        }

        if (owner instanceof Source) {
            return DynamicValueType.SRC;
        }

        if (owner instanceof Target) {
            return DynamicValueType.TAR;
        }

        if (owner instanceof Application) {
            return DynamicValueType.VAR;
        }

        return DynamicValueType.MAP;
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

    public boolean print(OutputConfig outputConfig) {
        DynamicValueType tableType = getTableType();

        List<OutputTypes> outputTypeList = outputConfig.getOutputTypeList();
        if (outputTypeList.size() == 0) {
            log.debug("no output config for {}:{}", tableType.name(), name);
            return false;
        }

        boolean success = true;
        boolean exitOnError = application.exitOnError;
        for (OutputTypes outputType : outputTypeList) {
            log.trace("printing {}:{} to Output({})", tableType.name(), name, outputType.name());
            if (!OutputFactory.getOutput(application, outputType).print(outputConfig, this)) {
                success = false;
                if (exitOnError) {
                    return false;
                }
            }
        }

        return success;
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
