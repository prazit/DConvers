package com.clevel.dconvers.input;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ResultSetMetaDataSource extends DataSource {

    public ResultSetMetaDataSource(DConvers dconvers, String name, DataSourceConfig dataSourceConfig) {
        super(dconvers, name, dataSourceConfig);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ResultSetMetaDataSource.class);
    }

    @Override
    public boolean open() {
        // nothing here, open file is in getDataTable function.
        return true;
    }

    @Override
    public void close() {
        // nothing here, close file is in getDataTable function.
    }

    @Override
    public DataTable getDataTable(String tableName, String idColumnName, String query, HashMap<String, String> queryParamMap) {
        DataTable dataTable = new DataTable(dconvers, tableName, idColumnName);
        Converter currentConverter = dconvers.currentConverter;
        currentConverter.setCurrentTable(dataTable);
        dataTable.setDataSource(name);
        dataTable.setQuery(query);

        DataTable metaDataTable = currentConverter.getDataTable(query);
        if (metaDataTable == null) {
            error("Invalid query({}) for ResultSetMetaData, please check source.{}.query", query, tableName);
            return dataTable;
        }

        DataTable metaData = metaDataTable.getMetaData();
        if (metaData == null) {
            error("No Meta-Data for query({}), please check source.{}.query", query, tableName);
            return dataTable;
        }

        dataTable.setRowList(metaData.getRowList());
        return dataTable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("name", name)
                .toString();
    }
}
