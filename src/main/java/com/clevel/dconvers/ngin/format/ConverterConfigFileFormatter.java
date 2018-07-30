package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;

import java.util.List;

public class ConverterConfigFileFormatter extends DataFormatter {

    public ConverterConfigFileFormatter() {
        super(true);
    }

    @Override
    protected String format(DataRow row) {
        List<DataColumn> columnList = row.getColumnList();
        String tableName = "";
        String generated = "";
        String name;
        String value;

        for (DataColumn column : columnList) {
            name = column.getName();
            value = column.getValue();
            if (name.endsWith("table")) {
                tableName = value;
            }
            generated += name + "=" + value + "\n";
        }

        String result = "\n\n# " + (columnList.size() - 9) + " columns from table '" + tableName + "'\n" + generated;
        return result;
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        return "\n#EOF";
    }
}
