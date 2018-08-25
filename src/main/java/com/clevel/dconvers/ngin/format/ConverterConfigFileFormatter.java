package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;

import java.util.List;

public class ConverterConfigFileFormatter extends DataFormatter {

    private String targets;
    private String sources;
    private String sqlCount;

    private Logger log;

    public ConverterConfigFileFormatter() {
        super(true);

        sources = "\n#-------sources-------\n\n\n";
        targets = "#-------targets-------\n";
        sqlCount = "#-------rowcount-------\n\n\n";
    }

    @Override
    protected String format(DataRow row) {


        List<DataColumn> columnList = row.getColumnList();
        String tableName = "";
        String generated = "";
        String name;
        String value;
        String idKey = "";
        String id = "";

        for (DataColumn column : columnList) {
            name = column.getName();
            value = column.getValue();
            if (name.endsWith(".table")) {
                tableName = value.replace("\"", "");
                idKey = Property.TARGET.connectKey(tableName, Property.ID);
            } else if (name.equals(idKey)) {
                id = value;
            }
            generated += name + "=" + value + "\n";
        }
        targets += "\n\n# " + (columnList.size() - 9) + " columns from table '" + tableName + "'\n" + generated;

        String query = "SELECT * FROM " + tableName + " ORDER BY " + id;
        String sourceKey = Property.SOURCE.connectKey(tableName);
        sources += Property.SOURCE.key() + "=" + tableName + "\n"
                + sourceKey + "." + Property.DATA_SOURCE + "=<datasource>\n"
                + sourceKey + "." + Property.ID + "=" + id + "\n"
                + sourceKey + "." + Property.QUERY + "=" + query + "\n\n\n";

        sqlCount += "SELECT '" + tableName + "' as TABLE_NAME, COUNT(" + tableName + "." + id + ") as ROWCOUNT FROM " + tableName + " UNION ";

        return null;
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        sqlCount = sqlCount.substring(0, sqlCount.length() - 7) + ";\n\n\n";
        return sqlCount + sources + targets + "#EOF";
    }
}
