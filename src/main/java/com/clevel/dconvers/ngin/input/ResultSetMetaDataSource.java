package com.clevel.dconvers.ngin.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSetMetaDataSource extends DataSource {

    public ResultSetMetaDataSource(Application application, String name, DataSourceConfig dataSourceConfig) {
        super(application, name, dataSourceConfig);
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
    public DataTable getDataTable(String tableName, String idColumnName, String query) {
        DataTable metaDataTable = application.currentConverter.getDataTable(query);
        if (metaDataTable == null) {
            error("Invalid query({}) for ResultSetMetaData, please check source.{}.query", query, tableName);
            return null;
        }

        DataTable metaData = metaDataTable.getMetaData();
        if (metaData == null) {
            error("No Meta-Data for query({}), please check source.{}.query", query, tableName);
            return null;
        }

        DataTable dataTable = new DataTable(application, tableName, idColumnName);
        dataTable.setDataSource(name);
        dataTable.setQuery(query);
        dataTable.setRowList(metaData.getRowList());

        return dataTable;
    }

}
