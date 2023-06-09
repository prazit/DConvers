package com.clevel.dconvers.format;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConverterSourceFormatter extends DataFormatter {

    private String eol;
    private String eof;
    private String doubleEOL;
    private String owner;
    private String table;
    private String id;
    private String dataSource;
    private String[] outputTypes;
    private int sourceIndex;
    private String nowString;
    private boolean isFirstRow;

    private String sqlCount;
    private String truncateTables;

    public ConverterSourceFormatter(DConvers dconvers, String name, OutputConfig outputConfig) {
        super(dconvers, name, true);

        eol = outputConfig.getSrcOutputEOL();
        eof = outputConfig.getSrcOutputEOF();
        doubleEOL = eol + eol;

        owner = outputConfig.getSrcOwner();
        table = outputConfig.getSrcTable();
        id = outputConfig.getSrcId();
        dataSource = outputConfig.getSrcDataSource();
        outputTypes = outputConfig.getSrcOutputs().split("[,]");
        sourceIndex = 0;
        isFirstRow = true;

        nowString = dconvers.systemVariableMap.get(SystemVariable.NOW).getValue();

        sqlCount = "";
        truncateTables = "";
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        return "# generated by DConvers @" + nowString + eol + eol;
    }

    @Override
    public String format(DataRow row) {
        sourceIndex++;
        String ownerName = getColumnString(row, owner);
        String tableName = getColumnString(row, table);
        String idColumnName = getColumnString(row, id);
        String query = "SELECT * FROM " + tableName + " ORDER BY " + idColumnName;

        String sourceName = tableName;
        String sourceKey = Property.SOURCE.connectKey(sourceName);

        String generated = eol + "#------- SRC:" + sourceName + " -------" + doubleEOL
                + Property.SOURCE.key() + "=" + sourceName + eol
                + sourceKey + "." + Property.INDEX.key() + "=" + String.valueOf(sourceIndex) + eol
                + sourceKey + "." + Property.DATA_SOURCE.key() + "=" + dataSource + eol
                + sourceKey + "." + Property.QUERY.key() + "=" + query + eol
                + sourceKey + "." + Property.ID.key() + "=" + idColumnName;

        String outputKey = "." + Property.OUTPUT_FILE.key();
        for (String outputType : outputTypes) {
            if (outputType.contains(".")) {
                generated += eol + sourceKey + "." + outputType + "=true";
            } else {
                generated += doubleEOL + sourceKey + "." + outputType + "=true"
                        + eol + sourceKey + "." + outputType + outputKey + "=" + outputType + "/V$[VAR:SOURCE_FILE_NUMBER]__$[CAL:NAME(CURRENT)]." + getFileExtension(outputType);
                if (outputType.equalsIgnoreCase("SQL")) {
                    generated += eol + sourceKey + "." + outputType + ".table=" + tableName;
                }
            }
        }
        generated += eol;

        /*if (isFirstRow) {
            sourceKey += ".tar";
            generated += eol + "# To generate target for all tables in current converter" + eol
                    + sourceKey + "=true" + eol
                    + "#" + sourceKey + "." + Property.OUTPUT_FILE.key() + "=<replace-this-by-output-file-name>" + eol
                    + sourceKey + "." + Property.OUTPUT_TYPES.key() + "=sql,markdown" + eol;
        }*/
        isFirstRow = false;

        sqlCount += "SELECT '" + tableName + "' as TABLE_NAME, COUNT(" + tableName + "." + idColumnName + ") as ROWCOUNT FROM " + tableName + " UNION ";

        truncateTables += "TRUNCATE TABLE " + tableName + ";" + eol;

        return generated;
    }

    private String getFileExtension(String outputType) {
        outputType = outputType.toUpperCase();

        if (outputType.equals("MARKDOWN")) {
            return "md";
        }

        if (outputType.equals("SRC") || outputType.equals("TAR")) {
            return "conf";
        }

        return outputType.toLowerCase();
    }

    private String getColumnString(DataRow row, String columnName) {
        DataColumn dataColumn = row.getColumn(columnName);
        if (dataColumn == null) {
            return "";
        }
        return dataColumn.getValue();
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        sqlCount = sqlCount.substring(0, sqlCount.length() - 7) + ";" + eol;

        return eol + "#------- Count row of all tables -------" + doubleEOL + sqlCount
                + eol + "#------- Truncate all tables -------" + doubleEOL + truncateTables;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConverterSourceFormatter.class);
    }

}
