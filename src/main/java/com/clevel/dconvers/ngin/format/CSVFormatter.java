package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class CSVFormatter extends DataFormatter {

    private boolean header;
    private String separator;
    private String nullString;
    private String eol;
    private String eof;
    private String datetimeFormat;
    private String integerFormat;
    private String decimalFormat;
    private String stringFormat;

    public CSVFormatter(Application application, String name, OutputConfig outputConfig) {
        super(application, name, true);

        header = outputConfig.isCsvHeader();
        separator = notNull(outputConfig.getCsvSeparator(), "");
        nullString = notNull(outputConfig.getCsvNullString(), "");
        eol = notNull(outputConfig.getCsvOutputEOL(), "");
        eof = notNull(outputConfig.getCsvOutputEOF(), "");
        integerFormat = notNull(outputConfig.getCsvFormatInteger(), this.integerFormat);
        decimalFormat = notNull(outputConfig.getCsvFormatDecimal(), this.decimalFormat);
        datetimeFormat = notNull(outputConfig.getCsvFormatDatetime(), this.datetimeFormat);
        stringFormat = notNull(outputConfig.getCsvFormatString(), this.stringFormat);

        outputType = "CSV";
    }

    private String notNull(String value, String replaceNull) {
        return value == null ? replaceNull : value;
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        if (!header || dataTable.getRowCount() == 0) {
            return null;
        }

        String csvLine = "";
        for (DataColumn dataColumn : dataTable.getRow(0).getColumnList()) {
            csvLine += dataColumn.getName();
            csvLine += separator;
        }
        csvLine = csvLine.substring(0, csvLine.length() - separator.length() - 1);

        return csvLine + eol;
    }

    @Override
    public String format(DataRow row) {
        String csvLine = "";
        int columnIndex = -1;
        String formatted;

        for (DataColumn dataColumn : row.getColumnList()) {
            columnIndex++;

            dataColumn.setIndex(columnIndex);
            dataColumn.setNullString(nullString);
            formatted = dataColumn.getFormattedValue(getFormatPattern(dataColumn));

            csvLine += formatted;
            csvLine += separator;
        }

        csvLine = csvLine.substring(0, csvLine.length() - separator.length() - 1);
        return csvLine + eol;

    }

    private String getFormatPattern(DataColumn dataColumn) {
        switch (dataColumn.getType()) {
            case Types.DATE:
            case Types.TIMESTAMP:
                return datetimeFormat;

            case Types.DECIMAL:
                return decimalFormat;

            case Types.INTEGER:
                return integerFormat;

            default: /*case Types.VARCHAR:*/
                return stringFormat;
        }
    }

    @Override
    protected boolean allowToWrite(StringBuffer stringBuffer) {

        int eolLength = eol.length();
        int bufferLength = stringBuffer.length();

        stringBuffer.delete(bufferLength - eolLength, bufferLength);
        stringBuffer.append(eof);

        log.debug("CSVFormatter.allowToWrite. eol({}), eof({}), lengthBefore({}), lengthAfter({})", eol, eof, bufferLength, stringBuffer.length());
        return super.allowToWrite(stringBuffer);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(CSVFormatter.class);
    }
}