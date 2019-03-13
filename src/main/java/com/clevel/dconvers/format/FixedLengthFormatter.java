package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.output.LengthMode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FixedLengthFormatter extends DataFormatter {

    private List<String> txtTypeList;
    private List<BigDecimal> txtLengthList;
    private String separator;
    private String eol;
    private String eof;
    private String dateFormat;
    private String datetimeFormat;
    private String decimalFormat = "#0.00";
    private String integerFormat = "#0";
    private String fillString;
    private String fillNumber;
    private String fillDate;
    private String charset;
    private boolean lengthInBytes;

    public FixedLengthFormatter(Application application, String name, String format, String lengthMode, String separator, String eol, String eof, String charset, String dateFormat, String datetimeFormat, String fillString, String fillNumber, String fillDate) {
        super(application, name, true);
        txtTypeList = new ArrayList<>();
        txtLengthList = new ArrayList<>();
        extractTxtFormat(format, txtTypeList, txtLengthList);

        this.lengthInBytes = isLengthInBytes(lengthMode);
        this.separator = notNull(separator, "");
        this.eol = notNull(eol, "");
        this.eof = notNull(eof, "");
        this.charset = notNull(charset, "UTF-8");
        this.dateFormat = notNull(dateFormat, Defaults.DATE_FORMAT.getStringValue());
        this.datetimeFormat = notNull(datetimeFormat, this.dateFormat);
        this.fillString = notNull(fillString, " ");
        this.fillNumber = notNull(fillNumber, "0");
        this.fillDate = notNull(fillDate, " ");
        outputType = "Fixed Length";

        log.debug("FixedLengthFormatter is created with LengthMode({})", lengthMode);
    }

    public FixedLengthFormatter(Application application, String name, OutputConfig outputConfig) {
        super(application, name, true);
        txtTypeList = new ArrayList<>();
        txtLengthList = new ArrayList<>();
        extractTxtFormat(outputConfig.getTxtFormat(), txtTypeList, txtLengthList);

        String txtLengthMode = outputConfig.getTxtLengthMode();
        lengthInBytes = isLengthInBytes(txtLengthMode);
        separator = notNull(outputConfig.getTxtSeparator(), "");
        eol = notNull(outputConfig.getTxtOutputEOL(), "");
        eof = notNull(outputConfig.getTxtOutputEOF(), "");
        charset = notNull(outputConfig.getTxtOutputCharset(), "UTF-8");
        dateFormat = notNull(outputConfig.getTxtFormatDate(), Defaults.DATE_FORMAT.getStringValue());
        datetimeFormat = notNull(outputConfig.getTxtFormatDatetime(), this.dateFormat);
        fillString = notNull(outputConfig.getTxtFillString(), " ");
        fillNumber = notNull(outputConfig.getTxtFillNumber(), "0");
        fillDate = notNull(outputConfig.getTxtFillDate(), " ");
        outputType = "Fixed Length";

        log.debug("FixedLengthFormatter is created with LengthMode({})", txtLengthMode);
    }

    private boolean isLengthInBytes(String txtLengthMode) {
        LengthMode lengthMode = LengthMode.parse(txtLengthMode);
        if (lengthMode == null) {
            return Defaults.TXT_LENGTH_IN_BYTES.getBooleanValue();
        }
        return lengthMode.equals(LengthMode.BYTE);
    }

    private String notNull(String value, String replaceNull) {
        return value == null ? replaceNull : value;
    }

    private void extractTxtFormat(String txtFormat, List<String> txtTypeList, List<BigDecimal> txtLengthList) {
        int index = -1;
        txtLengthList.clear();
        txtTypeList.clear();

        String[] columns = txtFormat.split("[,]");
        BigDecimal bigDecimal;
        for (String column : columns) {
            String[] values = column.split("[:]");
            index++;
            txtTypeList.add(index, values[0]);
            if (values.length > 1) {
                bigDecimal = new BigDecimal(values[1]);
            } else {
                bigDecimal = BigDecimal.ZERO;
            }
            txtLengthList.add(index, bigDecimal);
        }
    }

    @Override
    public String format(DataRow row) {
        String txtLine = "";
        int columnIndex = -1;
        int columns = txtTypeList.size();
        String formatted;

        for (DataColumn dataColumn : row.getColumnList()) {
            columnIndex++;
            if (columnIndex >= columns) {
                log.warn("FixedLength: might need specified format for column(index:{},name:{}) in '{}'.", columnIndex, dataColumn.getName(), name);
                application.hasWarning = true;
                break;
            }

            dataColumn.setIndex(columnIndex);
            formatted = format(dataColumn);

            txtLine += formatted;
            txtLine += separator;
        }

        txtLine = txtLine.substring(0, txtLine.length() - separator.length() - separator.length());
        return txtLine + eol;
    }

    private String format(DataColumn dataColumn) {
        int columnIndex = dataColumn.getIndex();
        String columnName = dataColumn.getName();
        int columnType = dataColumn.getType();
        BigDecimal txtLength = txtLengthList.get(columnIndex);
        if (txtLength.equals(BigDecimal.ZERO)) {
            log.debug("Column({}) is ignored by specific-length(0).", columnName);
            return "";
        }

        DynamicValueType txtType = DynamicValueType.parse(txtTypeList.get(columnIndex));
        String formatted;
        String value;

        switch (txtType) {
            case DTE:
                dataColumn.setNullString(fillDate);
                if (columnType == Types.DATE) {
                    value = dataColumn.getFormattedValue(dateFormat);
                } else {
                    value = dataColumn.getValue();
                }
                formatted = fixedLengthStringInCharacters(value, txtLength.intValue(), columnName);
                break;

            case DTT:
                dataColumn.setNullString(fillDate);
                if (columnType == Types.DATE) {
                    value = dataColumn.getFormattedValue(datetimeFormat);
                } else {
                    value = dataColumn.getValue();
                }
                formatted = fixedLengthStringInCharacters(value, txtLength.intValue(), columnName);
                break;

            case DEC:
                dataColumn.setNullString(fillNumber);
                if (columnType == Types.DECIMAL) {
                    value = dataColumn.getFormattedValue(decimalFormat);
                } else if (columnType == Types.INTEGER) {
                    value = dataColumn.getFormattedValue(integerFormat);
                } else {
                    value = dataColumn.getValue();
                }
                formatted = fixedLengthDecimal(value, txtLength, columnName);
                break;

            case INT:
                dataColumn.setNullString(fillNumber);
                if (columnType == Types.DECIMAL || columnType == Types.INTEGER) {
                    value = dataColumn.getFormattedValue(integerFormat);
                } else {
                    value = dataColumn.getValue();
                }
                formatted = fixedLengthInteger(value, txtLength.intValue(), columnName);
                break;

            default: // case STR:
                value = dataColumn.getValue();
                if (value.equalsIgnoreCase("null")) {
                    value = fillString;
                }
                if (lengthInBytes) {
                    formatted = fixedLengthStringInBytes(value, txtLength.intValue(), columnName);
                } else {
                    formatted = fixedLengthStringInCharacters(value, txtLength.intValue(), columnName);
                }
        }

        log.debug("format(type:{},value:{}) = {}({})", dataColumn.getType(), value, txtType, formatted);
        return formatted;
    }

    /**
     * Use for string and date.
     */
    private String fixedLengthStringInBytes(String stringValue, int targetLength, String columnName) {
        log.debug("fixedLengthString(value:{}, length:{}, name:{})", stringValue == null ? "null" : "\"" + stringValue + "\"", targetLength, columnName);

        byte[] byteArray;
        try {
            byteArray = stringValue.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            log.warn("String value of column({}) report about Unsupported Encoding for charset={}", columnName, charset);
            byteArray = stringValue.getBytes();
        }

        String formatted;
        int already = byteArray.length;

        if (targetLength > already) {
            int count = targetLength - already;
            formatted = fillRight(stringValue, fillString, count);
        } else if (already > targetLength) {
            formatted = new String(ArrayUtils.subarray(byteArray, 0, targetLength));
        } else {
            formatted = stringValue;
        }

        return formatted;
    }


    /**
     * Use for string and date.
     */
    private String fixedLengthStringInCharacters(String stringValue, int targetLength, String columnName) {
        String formatted;
        int already = stringValue.length();

        if (targetLength > already) {
            int count = targetLength - already;
            formatted = fillRight(stringValue, fillString, count);
        } else if (already > targetLength) {
            formatted = stringValue.substring(0, targetLength);
        } else {
            formatted = stringValue;
        }

        return formatted;
    }

    private String fixedLengthInteger(String integerAsStringValue, int targetLength, String columnName) {
        String formatted;
        int already = integerAsStringValue.length();

        if (targetLength > already) {
            int count = targetLength - already;
            formatted = fillLeft(integerAsStringValue, fillNumber, count);
        } else if (already > targetLength) {
            formatted = integerAsStringValue.substring(already - targetLength, already);

            Logger log = LoggerFactory.getLogger(FixedLengthFormatter.class);
            log.warn("Integer value of column({}) has broken by the fixed length({})! OriginalValue({}) OutputValue({})", columnName, formatted.length(), integerAsStringValue, formatted);
            application.hasWarning = true;
        } else {
            formatted = integerAsStringValue;
        }

        return formatted;
    }

    private String fixedLengthDecimal(String decimalAsStringValue, BigDecimal decimalLength, String columnName) {
        log.debug("fixedLengthDecimal(decimalAsStringValue:{}, decimalLength:{}, columnName:{})", decimalAsStringValue, decimalLength, columnName);

        if (decimalLength.equals(BigDecimal.ZERO)) {
            return "";
        }

        boolean warning = false;

        // length = 11.2, targetLength = 11, right = 2 then left = 9
        int targetLength = decimalLength.intValue();
        int right = decimalLength.subtract(BigDecimal.valueOf(targetLength)).intValue();
        int left = targetLength - right;

        String[] decimal = decimalAsStringValue.split(".");
        String formatted;
        int usedLeft;
        int usedRight;
        if (decimal.length < 2) {
            usedLeft = decimal[0].length();
            usedRight = 0;
        } else {
            usedLeft = decimal[0].length();
            usedRight = decimal[1].length();
        }

        // left
        if (left > usedLeft) {
            int count = left - usedLeft;
            formatted = fillLeft(decimal[0], fillNumber, count);
        } else if (usedLeft > left) {
            formatted = decimal[0].substring(usedLeft - left, usedLeft);

            warning = true;
        } else {
            formatted = decimal[0];
        }

        // right
        if (right > usedRight) {
            int count = right - usedRight;
            formatted += fillRight(decimal[1], "0", count);     // fraction need to fill by "0" only.
        } else if (usedRight > right) {
            formatted += decimal[1].substring(usedRight - right, usedRight);
        } else {
            formatted += decimal[1];
        }

        if (warning) {
            application.hasWarning = true;
            Logger log = LoggerFactory.getLogger(FixedLengthFormatter.class);
            log.warn("Decimal value of column({}) has broken by the fixed length! OriginalValue({}) OutputValue({})", columnName, decimalAsStringValue, formatted);
        }

        return formatted;
    }

    private String fillLeft(String value, String fill, int count) {
        return StringUtils.repeat(fill, count).concat(value);
    }

    private String fillRight(String value, String fill, int count) {
        return value.concat(StringUtils.repeat(fill, count));
    }

    @Override
    protected boolean allowToWrite(StringBuffer stringBuffer) {

        int eolLength = eol.length();
        int bufferLength = stringBuffer.length();

        stringBuffer.delete(bufferLength - eolLength, bufferLength);
        stringBuffer.append(eof);

        log.debug("FixedLengthFormatter.allowToWrite. eol({}), eof({}), lengthBefore({}), lengthAfter({})", eol, eof, bufferLength, stringBuffer.length());
        return super.allowToWrite(stringBuffer);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(FixedLengthFormatter.class);
    }
}