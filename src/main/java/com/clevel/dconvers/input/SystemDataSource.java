package com.clevel.dconvers.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.*;

public class SystemDataSource extends DataSource {

    public SystemDataSource(Application application, String name, DataSourceConfig dataSourceConfig) {
        super(application, name, dataSourceConfig);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SystemDataSource.class);
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

        SystemQuery systemQuery = SystemQuery.parse(query);
        if (systemQuery == null) {
            error("Invalid system-query({}) for system datasource, please check source.{}.query", query, tableName);
            return null;
        }

        switch (systemQuery) {
            case ARG:
                return arg(tableName, idColumnName);

            case VARIABLE:
                return systemVariables(tableName, idColumnName);

            case ENVIRONMENT:
                return systemProperties(tableName, idColumnName);

            case SUMMARY:
                return application.summaryTable;
        }

        return super.getDataTable(tableName, idColumnName, query);
    }

    private DataTable arg(String tableName, String idColumnName) {
        String arg = application.switches.getArg();
        if (arg == null) {
            return null;
        }

        DataTable dataTable = new DataTable(application, tableName, idColumnName);
        dataTable.setDataSource(name);
        dataTable.setQuery(SystemQuery.ARG.name());
        DataRow dataRow;
        String columnName;

        String[] args = arg.split("[,]");
        int index = 0;
        for (String argument : args) {
            index++;

            dataRow = new DataRow(application, dataTable);

            columnName = "INDEX";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(index)));

            columnName = "VALUE";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, argument));

            dataTable.addRow(dataRow);
        }

        return dataTable;
    }

    private DataTable systemVariables(String tableName, String idColumnName) {
        HashMap<SystemVariable, DataColumn> systemVariableMap = application.systemVariableMap;

        DataTable dataTable = new DataTable(application, tableName, idColumnName);
        dataTable.setDataSource(name);
        dataTable.setQuery(SystemQuery.VARIABLE.name());
        DataRow dataRow;
        String columnName;

        List<DataColumn> variables = new ArrayList<>(systemVariableMap.values());
        variables.sort(Comparator.comparing(DataColumn::getName));
        for (DataColumn variable : variables) {

            dataRow = new DataRow(application, dataTable);

            columnName = "VAR";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, variable.getName()));

            columnName = "TYPE";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(variable.getType())));

            columnName = "VALUE";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, variable.getType(), variable.getValue()));

            dataTable.addRow(dataRow);
        }

        return dataTable;
    }

    private DataTable systemProperties(String tableName, String idColumnName) {

        DataTable dataTable = new DataTable(application, tableName, idColumnName);
        dataTable.setDataSource(name);
        dataTable.setQuery(SystemQuery.ENVIRONMENT.name());
        DataRow dataRow;
        String columnName;

        Properties properties = System.getProperties();
        List<String> propertyList = new ArrayList<>(properties.stringPropertyNames());
        propertyList.sort(String::compareTo);
        for (String propertyName : propertyList) {
            dataRow = new DataRow(application, dataTable);

            columnName = "PROPERTY";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, propertyName));

            columnName = "VALUE";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, System.getProperty(propertyName)));

            dataTable.addRow(dataRow);
        }

        return dataTable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("name", name)
                .toString();
    }
}
