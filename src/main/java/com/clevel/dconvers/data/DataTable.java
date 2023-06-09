package com.clevel.dconvers.data;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.ngin.AppBase;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.Target;
import com.clevel.dconvers.output.OutputFactory;
import com.clevel.dconvers.output.OutputTypes;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataTable extends AppBase {

    protected Object owner;

    /* dataRowMap is HashMap<ColumnName,HashMap<ValueString,DataRow>>
     * ValueString is DataColumn.getValue()
     **/
    protected HashMap<String, HashMap<String, DataRow>> dataRowMap;
    protected List<DataRow> dataRowList;
    protected String idColumnName;

    protected DataTable metaData;
    protected String dataSource;
    protected String query;

    public DataTable(DConvers dconvers, String tableName, String idColumnName) {
        super(dconvers, tableName);

        this.idColumnName = idColumnName;
        this.owner = null;
        dataRowList = new ArrayList<>();
        dataRowMap = new HashMap<>();
        valid = true;
        dataSource = "";
        query = "";
    }

    public DataTable(DConvers dconvers, String tableName, String idColumnName, Object owner) {
        super(dconvers, tableName);

        this.idColumnName = idColumnName;
        this.owner = owner;
        dataRowList = new ArrayList<>();
        dataRowMap = new HashMap<>();
        valid = true;
        dataSource = "";
        query = "";
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
            return new DataRow(dconvers, this);
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

    public DataRow getRow(List<String> keyList, List<String> valueList) {
        DataRow dataRow = getRow(keyList.get(0), valueList.get(0));
        if (dataRow == null) {
            return null;
        }

        DataColumn dataColumn;
        int size = keyList.size();
        for (int index = 1; index < size; index++) {
            dataColumn = dataRow.getColumn(keyList.get(index));
            if (dataColumn == null) {
                return null;
            }

            if (!valueList.get(index).equals(dataColumn.getValue())) {
                return null;
            }

        }

        return dataRow;
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

        if (owner instanceof DConvers) {
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
                .append("metaData", metaData)
                .toString()
                .replace('=', ':');
    }

    public boolean print(OutputConfig outputConfig) {
        DynamicValueType tableType = getTableType();
        if (outputConfig == null || !outputConfig.isValid()) {
            log.debug("no output config for {}:{}", tableType.name(), name);
            return true;
        }

        List<OutputTypes> outputTypeList = outputConfig.getOutputTypeList();
        if (outputTypeList.size() == 0) {
            log.debug("no output config for {}:{}", tableType.name(), name);
            return true;
        }

        boolean success = true;
        boolean exitOnError = dconvers.exitOnError;
        String outputName;
        for (OutputTypes outputType : outputTypeList) {
            log.debug("printing {}:{} to Output({})", tableType.name(), name, outputType.name());
            outputName = OutputFactory.getOutput(dconvers, outputType).print(outputConfig, this);
            if (outputName == null) {
                success = false;
                if (exitOnError) {
                    return false;
                }
            }
            dconvers.outputSummary.addRow(dconvers.currentConverter.getName(), getTableType(), name, outputType, outputName, getRowCount());
        }

        return success;
    }

    public DataTable clone() {
        DataTable dataTable = new DataTable(dconvers, name, idColumnName);
        dataTable.setOwner(owner);
        dataTable.setQuery(query);
        dataTable.setDataSource(dataSource);
        dataTable.setMetaData(metaData);
        dataTable.setRowList(dataRowList);
        return dataTable;
    }


}
