package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.FixedLengthFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FixedLengthTransform extends Transform {

    public FixedLengthTransform(Application application, String name) {
        super(application, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        String format = getArgument(Property.ARGUMENTS.key());
        String newColumnArgsString = getFirstValue(format);
        format = format.substring(newColumnArgsString.length() + 1);

        String[] newColumnArgs = newColumnArgsString.split("[:]");
        String newColumnName = newColumnArgs[0];
        int newColumnIndex = Integer.valueOf(newColumnArgs[1]) - 1;

        String separator = getArgument(Property.SEPARATOR.key());
        String dateFormat = getArgument(Property.FORMAT_DATE.key(), "YYYYMMdd");
        String datetimeFormat = getArgument(Property.FORMAT_DATETIME.key(), "YYYYMMddHHmmss");
        String fillString = getArgument(Property.FILL_STRING.key()," ");
        String fillNumber = getArgument(Property.FILL_NUMBER.key(), "0");
        String fillDate = getArgument(Property.FILL_DATE.key(), " ");
        FixedLengthFormatter fixedLengthFormatter = new FixedLengthFormatter(application, name, format, separator, "", dateFormat, datetimeFormat, fillString, fillNumber, fillDate);

        List<DataRow> newRowList = new ArrayList<>();
        List<DataRow> rowList = dataTable.getAllRow();

        String formatted;

        for (DataRow row : rowList) {
            formatted = fixedLengthFormatter.format(row);
            newRowList.add(insertReplaceColumn(row, newColumnName, newColumnIndex, formatted));
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