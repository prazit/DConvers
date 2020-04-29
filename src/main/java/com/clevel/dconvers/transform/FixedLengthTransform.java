package com.clevel.dconvers.transform;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.FixedLengthFormatter;
import com.clevel.dconvers.output.LengthMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FixedLengthTransform extends Transform {

    public FixedLengthTransform(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        String format = getArgument(Property.ARGUMENTS.key());
        String newColumnArgsString = getFirstValue(format);
        format = format.substring(newColumnArgsString.length() + 1);

        String[] newColumnArgs = newColumnArgsString.split("[:]");
        String newColumnName = newColumnArgs[0];
        int newColumnIndex = Integer.parseInt(newColumnArgs[1]) - 1;

        String separator = getArgument(Property.SEPARATOR.key());
        String charset = getArgument(Property.OUTPUT_CHARSET.key(), "UTF-8");
        String dateFormat = getArgument(Property.FORMAT_DATE.key(), "yyyyMMdd");
        String datetimeFormat = getArgument(Property.FORMAT_DATETIME.key(), "yyyyMMddHHmmss");
        String fillString = getArgument(Property.FILL_STRING.key(), " ");
        String fillNumber = getArgument(Property.FILL_NUMBER.key(), "0");
        String fillDate = getArgument(Property.FILL_DATE.key(), " ");
        String lengthMode = getArgument(Property.LENGTH_MODE.key(), LengthMode.CHAR.name());

        FixedLengthFormatter fixedLengthFormatter = new FixedLengthFormatter(dconvers, name, format, lengthMode, separator, "", "", charset, dateFormat, datetimeFormat, fillString, fillNumber, fillDate);

        List<DataRow> newRowList = new ArrayList<>();
        List<DataRow> rowList = dataTable.getRowList();

        String formatted;
        DataRow newRow;

        for (DataRow row : rowList) {
            formatted = fixedLengthFormatter.format(row);
            newRow = insertReplaceColumn(row, newColumnName, newColumnIndex, new DataString(dconvers, 0, Types.VARCHAR, newColumnName, formatted));
            if (newRow == null) {
                return false;
            }
            newRowList.add(newRow);
        }

        dataTable.setRowList(newRowList);

        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(FixedLengthTransform.class);
    }

}
