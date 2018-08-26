package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;

import java.util.List;

public class ConverterConfigFileFormatter extends DataFormatter {

    private String dataSourceName;

    private String targets;
    private String sources;
    private String sqlCount;
    private String truncateTables;

    private long sourceNumber;

    private Logger log;

    public ConverterConfigFileFormatter() {
        super(true);

        sourceNumber = 0;
        sqlCount = "\n\n#-------SQL-------\n\n#-- Count row for all tables\n# ";
        truncateTables = "#-- Truncate for all tables\n# SET FOREIGN_KEY_CHECKS=0;";
        sources = "#-------sources-------\n\n\n";
        targets = "#-------targets-------\n";
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

        sourceNumber ++;
        String query = "SELECT * FROM " + tableName + " ORDER BY " + id;
        String sourceKey = Property.SOURCE.connectKey(tableName);
        sources += "# generated from table '" + tableName + "'\n"
                + Property.SOURCE.key() + "=" + tableName + "\n"
                + sourceKey + "." + Property.DATA_SOURCE.key() + "=" + dataSourceName + "\n"
                + sourceKey + "." + Property.ID.key() + "=" + id + "\n"
                + sourceKey + "." + Property.QUERY.key() + "=" + query + "\n"
                + sourceKey + "." + Property.CREATE.key() + "=false\n"
                + sourceKey + "." + Property.INSERT.key() + "=true\n"
                + sourceKey + "." + Property.MARKDOWN.key() + "=true\n"
                + sourceKey + "." + Property.INDEX.key() + "=" + String.valueOf(sourceNumber) + "\n\n\n";

        sqlCount += "SELECT '" + tableName + "' as TABLE_NAME, COUNT(" + tableName + "." + id + ") as ROWCOUNT FROM " + tableName + " UNION ";
        truncateTables += "TRUNCATE TABLE " + tableName + ";";

        return null;
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        sqlCount = sqlCount.substring(0, sqlCount.length() - 7) + ";\n";
        truncateTables =  truncateTables + "SET FOREIGN_KEY_CHECKS=1;\n\n\n";
        return sqlCount + truncateTables + sources + targets + "\n\n#EOF";
    }

    public void setDataSourceName(String name) {
        dataSourceName = name;
    }
}
