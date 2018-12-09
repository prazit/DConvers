package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class ConverterTargetFormatter extends DataFormatter {

    private String targets;
    private String sqlCount;
    private String truncateTables;

    private String eol;
    private String doubleEOL;
    private String eof;

    private String nowString;
    private String converterName;
    private int targetIndex;

    public ConverterTargetFormatter(Application application, String name, OutputConfig outputConfig) {
        super(application, name, false);

        targetIndex = 0;

        this.eol = outputConfig.getTarOutputEOL();
        this.eof = outputConfig.getTarOutputEOF();
        doubleEOL = eol + eol;

        nowString = application.systemVariableMap.get(SystemVariable.NOW).getValue();

        sqlCount = doubleEOL + "#------- SQL -------" + doubleEOL + "#-- Count row for all tables" + eol;
        truncateTables = doubleEOL + "#-- Truncate for all tables" + eol;
        targets = doubleEOL + "#------- Targets -------" + eol;
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        Converter converter = application.currentConverter;
        converterName = converter.getName();

        for (Source source : converter.getSourceList()) {
            sourceFormat(source.getDataTable());
        }

        return sqlCount + truncateTables + targets + eof;
    }

    @Override
    public String format(DataRow row) {
        return null;
    }

    /**
     * Generate one target for source datatable.
     */
    protected boolean sourceFormat(DataTable dataTable) {
        String tableName = dataTable.getName();
        if (dataTable.getRowCount() == 0) {
            log.warn("ConverterTargetFormatter.sourceFormat. source({}) does't have column prototype to generate target configuration!", tableName);
            application.hasWarning = true;
            return true;
        }

        targetIndex++;
        int columnCount = dataTable.getRow(0).getColumnCount();
        String name;
        int type;
        String value;
        String id = dataTable.getIdColumnName();

        String targetKey = Property.TARGET.connectKey(tableName);
        targets += doubleEOL + "# generated by DConvers @" + nowString + eol
                + "# " + String.valueOf(columnCount) + " columns from source(" + tableName + ") in converter(" + converterName + ")" + eol
                + Property.TARGET.key() + "=" + tableName + eol
                + targetKey + "." + Property.INDEX.key() + "=" + String.valueOf(targetIndex) + eol
                + targetKey + "." + Property.SOURCE.key() + "=" + tableName + eol
                + targetKey + "." + Property.ID.key() + "=" + id + eol;

        targetKey += ".column.";
        for (DataColumn column : dataTable.getRow(0).getColumnList()) {
            name = column.getName();
            type = column.getType();
            value = getDefaultValue(type);
            targets += targetKey + name + "=" + value + eol;
        }

        sqlCount += "# SELECT '" + tableName + "' as TABLE_NAME, COUNT(" + tableName + "." + id + ") as ROWCOUNT FROM " + tableName + " UNION ";
        sqlCount = sqlCount.substring(0, sqlCount.length() - 7) + ";" + eol;

        truncateTables += "# TRUNCATE TABLE " + tableName + ";" + eol;

        return true;
    }

    private String getDefaultValue(int columnType) {
        switch (columnType) {
            case Types.BIGINT:
            case Types.INTEGER:
                return "INT:0";

            case Types.DECIMAL:
                return "DEC:0.0";

            case Types.DATE:
            case Types.TIMESTAMP:
                return "DTE:NULL";

            default: // Types.VARCHAR:
                return "STR:NULL";
        }
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConverterTargetFormatter.class);
    }
}
