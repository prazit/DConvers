package com.clevel.dconvers.ngin.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.conf.SystemQuery;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.ngin.data.*;
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
                return arg();

            case VARIABLE:
                return systemVariables();

            case ENVIRONMENT:
                error("System Datasource: ENVIRONMENT is in construction phase.");
                break;
        }

        return super.getDataTable(tableName, idColumnName, query);
    }

    private DataTable arg() {
        String arg = application.switches.getArg();
        if (arg == null) {
            return null;
        }

        DataTable dataTable = new DataTable(application, "SYSTEM_ARG", "INDEX");
        DataRow dataRow;
        DataLong columnIndex;
        DataString columnArg;
        String columnName;

        String[] args = arg.split("[,]");
        int index = 0;
        for (String argument : args) {
            index++;

            dataRow = new DataRow(application, dataTable);

            columnName = "INDEX";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(index)));

            columnName = "ARG";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, argument));

            dataTable.addRow(dataRow);
        }

        return dataTable;
    }

    private DataTable systemVariables() {
        Map<SystemVariable, DataColumn> systemVariableMap = application.systemVariableMap;

        DataTable dataTable = new DataTable(application, "SYSTEM_ARG", "INDEX");
        DataRow dataRow;
        String columnName;

        int index = 0;
        List<DataColumn> variables = (List<DataColumn>) systemVariableMap.values();
        variables.sort(Comparator.comparing(DataColumn::getName));
        for (DataColumn variable : variables) {
            index++;

            dataRow = new DataRow(application, dataTable);

            columnName = "INDEX";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(index)));

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

}
