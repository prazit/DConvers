package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.calc.CalcTypes;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatTransform extends Transform {

    public FormatTransform(Application application, String name) {
        super(application, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        // format([replace or [ColumnName]]:[insertColumnIndex],[current or [[TableType]:[TableName]]],[current or [rowIndex]],[columnIndex or columnName])
        String arguments = getArgument(Property.ARGUMENTS.key());

        String columnIdentifier = getFirstValue(arguments);
        String[] columnIdentifiers = columnIdentifier.split("[:]");
        String newColumnName = columnIdentifiers[0];
        int newColumnIndex = Integer.parseInt(columnIdentifiers[1]) - 1;

        arguments = arguments.substring(columnIdentifier.length() + 1);
        return calcToRowList(dataTable, CalcTypes.FORMAT, arguments, dataTable, newColumnName, newColumnIndex);

    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(FormatTransform.class);
    }

}
