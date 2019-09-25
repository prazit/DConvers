package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVFormatter extends DataFormatter {

    private boolean header;
    private List<String> headerColumnList;

    private String separator;
    private String nullString;
    private String eol;
    private String eof;

    private String dateFormat;
    private String datetimeFormat;
    private String integerFormat;
    private String decimalFormat;
    private String stringFormat;

    private class CSVColumn {
        public String format;
        public String pattern;
        public int type;
        public int maxLength;

        public CSVColumn(String format) {
            setFormat(format);
        }

        private void setFormat(String format) {
            this.format = format;
            String[] part = format.split("[:]");
            type = DynamicValueType.parse(part[0]).getDataType();
            if (part.length == 1) {
                maxLength = 888;
            } else {
                maxLength = Integer.parseInt(part[1]);
            }
            pattern = getPattern(type);
        }

        public CSVColumn(DataColumn dataColumn) {
            switch (dataColumn.getType()) {
                case Types.DATE:
                    setFormat("DTE:10");
                    break;

                case Types.TIMESTAMP:
                    setFormat("DTT:21");
                    break;

                case Types.DECIMAL:
                    setFormat("DEC:20");
                    break;

                case Types.INTEGER:
                    setFormat("INT:20");
                    break;

                default: /*case Types.VARCHAR:*/
                    setFormat("STR:40");
            }
        }

        private String getPattern(int type) {
            switch (type) {
                case Types.DATE:
                    return dateFormat;

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
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("format", format)
                    .append("pattern", pattern)
                    .append("type", type)
                    .append("maxLength", maxLength)
                    .toString()
                    .replace('=', ':');
        }
    }/*end of class CSVColumn*/

    private List<CSVColumn> formatList;

    public CSVFormatter(Application application, String name, OutputConfig outputConfig) {
        super(application, name, true);

        header = outputConfig.isCsvHeader();
        headerColumnList = outputConfig.getCsvHeaderColumn();
        log.debug("CSVFormatter.constructor.headerColumnList = {}", Arrays.toString(headerColumnList.toArray()));

        separator = notNull(outputConfig.getCsvSeparator(), "");
        nullString = notNull(outputConfig.getCsvNullString(), "");
        eol = notNull(outputConfig.getCsvOutputEOL(), "");
        eof = notNull(outputConfig.getCsvOutputEOF(), "");

        setFormatList(outputConfig.getCsvFormat());
        integerFormat = notNull(outputConfig.getCsvFormatInteger(), this.integerFormat);
        decimalFormat = notNull(outputConfig.getCsvFormatDecimal(), this.decimalFormat);
        dateFormat = notNull(outputConfig.getCsvFormatDate(), this.dateFormat);
        datetimeFormat = notNull(outputConfig.getCsvFormatDatetime(), this.datetimeFormat);
        stringFormat = notNull(outputConfig.getCsvFormatString(), this.stringFormat);

        outputType = "CSV";
    }

    private void setFormatList(List<String> formatList) {
        this.formatList = new ArrayList<>();

        if (formatList.size() == 0) {
            return;
        }

        for (String format : formatList) {
            this.formatList.add(new CSVColumn(format));
        }
    }

    private String notNull(String value, String replaceNull) {
        return value == null ? replaceNull : value;
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        DataRow firstRow = dataTable.getRow(0);

        if (formatList.size() == 0) {
            // use default format by datatype of Column (for all columns).
            for (DataColumn dataColumn : firstRow.getColumnList()) {
                this.formatList.add(new CSVColumn(dataColumn));
            }
        } else if (formatList.size() < firstRow.getColumnCount()) {
            // use default format by datatype of Column (for some columns).
            int columnIndex = -1;
            int lastIndex = formatList.size() - 1;
            for (DataColumn dataColumn : firstRow.getColumnList()) {
                columnIndex ++;
                if (columnIndex > lastIndex) {
                    this.formatList.add(new CSVColumn(dataColumn));
                }
            }
        }
        log.debug("CSVFormat.preFormat: {}", formatList);

        /* need header line or not */
        if (!header) {
            return null;
        }

        String csvLine = "";
        if (headerColumnList.size() > 0) {
            for (String headerColumn : headerColumnList) {
                csvLine += headerColumn;
                csvLine += separator;
            }
        } else {
            for (DataColumn dataColumn : dataTable.getRow(0).getColumnList()) {
                csvLine += dataColumn.getName();
                csvLine += separator;
            }
        }
        csvLine = csvLine.substring(0, csvLine.length() - separator.length()) + eol;

        return csvLine;
    }

    @Override
    public String format(DataRow row) {
        String csvLine = "";
        int columnIndex = -1;
        String formatted;
        CSVColumn csvColumn;

        for (DataColumn dataColumn : row.getColumnList()) {
            columnIndex++;
            csvColumn = formatList.get(columnIndex);

            dataColumn.setIndex(columnIndex);
            dataColumn.setNullString(nullString);
            formatted = dataColumn.getFormattedValue(csvColumn.pattern);

            //log.debug("CSVFormatter.format: dataColumn={}", dataColumn.toString());
            //log.debug("CSVFormatter.format: csvColumn={}", csvColumn.toString());

            if (csvColumn.type == Types.VARCHAR) {
                /*max length of STR*/
                if (formatted.length() > csvColumn.maxLength) {
                    if (csvColumn.maxLength == 0) {
                        formatted = "";
                    } else {
                        formatted.substring(0, csvColumn.maxLength - 1);
                    }
                }
            }

            csvLine += formatted;
            csvLine += separator;
        }

        csvLine = csvLine.substring(0, csvLine.length() - separator.length());
        return csvLine + eol;
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