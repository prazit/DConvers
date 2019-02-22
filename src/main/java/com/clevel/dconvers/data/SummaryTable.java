package com.clevel.dconvers.data;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class SummaryTable extends DataTable {

    private long lastID;

    public SummaryTable(Application application) {
        super(application, "Summary", "id");
        lastID = 0;
    }

    @Deprecated
    @Override
    public void addRow(DataRow dataRow) {
        error("Deprecated function SummaryTable.addRow is invoked!");
    }

    public void addRow(String converterName, String tableName, DynamicValueType tableType, long rowCount) {
        log.info("{}:{} has {} row(s)", tableType.name(), tableName, rowCount);

        DataRow newRow = new DataRow(application, this);
        String columnName;
        lastID++;

        columnName = "ID";
        newRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(lastID)));

        columnName = "Table Type";
        newRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, tableType.name()));

        columnName = "Table Name";
        newRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, tableName));

        columnName = "Row Count";
        newRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(rowCount)));

        columnName = "Converter";
        newRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, converterName));

        super.addRow(newRow);
    }

    public void sort() {
        if (dataRowList.size() == 0) {
            return;
        }

        /*sort by Table Type, Table Name */
        dataRowList.sort((o1, o2) -> {
            String type1 = o1.getColumn("Table Type").getValue();
            String type2 = o2.getColumn("Table Type").getValue();
            int type = type1.compareTo(type2);
            if (type == 0) {

                String name1 = o1.getColumn("Table Name").getValue();
                String name2 = o2.getColumn("Table Name").getValue();
                return name1.compareTo(name2);

            }
            return type;
        });
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SummaryTable.class);
    }
}
