package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.calc.CalcTypes;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SumTransform extends Transform {

    public SumTransform(Application application, String name) {
        super(application, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        // sum([replace or [ColumnName]]:[insertColumnIndex],[current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..)
        String arguments = getArgument(Property.ARGUMENTS.key());

        String columnIdentifier = getFirstValue(arguments);
        String[] columnIdentifiers = columnIdentifier.split("[:]");
        String newColumnName = columnIdentifiers[0];
        int newColumnIndex = Integer.parseInt(columnIdentifiers[1]) - 1;

        arguments = arguments.substring(columnIdentifier.length() + 1);
        calcToRowList(dataTable.getAllRow(), CalcTypes.SUM, arguments, dataTable, newColumnName, newColumnIndex);

        return true;

    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SumTransform.class);
    }

}
