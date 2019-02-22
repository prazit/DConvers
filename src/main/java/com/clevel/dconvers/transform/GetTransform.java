package com.clevel.dconvers.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.calc.CalcTypes;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTransform extends Transform {

    public GetTransform(Application application, String name) {
        super(application, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        // get([replace or [ColumnName]]:[insertColumnIndex],[current or [[TableType]:[TableName]]],[current or [rowIndex]],[columnIndex])
        String arguments = getArgument(Property.ARGUMENTS.key());

        String columnIdentifier = getFirstValue(arguments);
        String[] columnIdentifiers = columnIdentifier.split("[:]");
        String newColumnName = columnIdentifiers[0];
        int newColumnIndex = Integer.parseInt(columnIdentifiers[1]) - 1;

        arguments = arguments.substring(columnIdentifier.length() + 1);
        return calcToRowList(dataTable, CalcTypes.GET, arguments, dataTable, newColumnName, newColumnIndex);

    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(GetTransform.class);
    }

}
