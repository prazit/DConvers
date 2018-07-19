package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;

public class ConverterConfigFileFormatter extends DataFormatter {

    public ConverterConfigFileFormatter() {
        super(true);
    }

    @Override
    protected String format(DataRow row) {
        String generated = "\n\n";

        for (DataColumn column : row.getColumnList()) {
            generated += column.getName() +"=";
            generated += column.getValue() +"\n";
        }

        return generated;
    }

}
