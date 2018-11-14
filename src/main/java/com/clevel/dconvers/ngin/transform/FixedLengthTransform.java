package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.Transform;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.FixedLengthFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FixedLengthTransform extends Transform {

    public FixedLengthTransform(Application application, String name) {
        super(application, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        String format = getArgument(Property.ARGUMENTS.key());
        String dateFormat = getArgument(Property.FORMAT_DATE.key());
        String datetimeFormat = getArgument(Property.FORMAT_DATETIME.key());
        String fillString = getArgument(Property.FILL_STRING.key());
        String fillNumber = getArgument(Property.FILL_NUMBER.key());
        String fillDate = getArgument(Property.FILL_DATE.key());
        FixedLengthFormatter fixedLengthFormatter = new FixedLengthFormatter(application, name, format, "", "", dateFormat, datetimeFormat, fillString, fillNumber, fillDate);

        List<DataRow> newRowList = new ArrayList<>();
        List<DataRow> rowList = dataTable.getAllRow();
        DataColumn newColumn;
        DataRow newRow;

        String columnName;
        String formatted;
        int columnIndex;

        for (DataRow row : rowList) {
            newRow = new DataRow(dataTable);
            columnIndex = -1;

            for (DataColumn column : row.getColumnList()) {
                columnIndex++;
                column.setIndex(columnIndex);

                formatted = fixedLengthFormatter.format(column);

                columnName = column.getName();
                newColumn = application.createDataColumn(columnName, Types.VARCHAR, formatted);
                newRow.putColumn(columnName, newColumn);
            }

            newRowList.add(newRow);
        }

        rowList.clear();
        rowList.addAll(newRowList);

        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(FixedLengthTransform.class);
    }

}
