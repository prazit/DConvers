package com.clevel.dconvers.data;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Defaults;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataDate extends DataColumn {
    private Date value;

    public DataDate(Application application, int index, int type, String name, Date value) {
        super(application, index, type, name);
        this.value = value;
    }

    public DataDate(Application application, int index, int type, String name, String value) {
        super(application, index, type, name);

        if (value == null) {
            this.value = null;
        } else {
            try {
                this.value = parse(value, Defaults.DATE_FORMAT.getStringValue());
            } catch (Exception e) {
                error("DataDate. parse date ({}) is failed.", value, e.getMessage());
                this.value = null;
            }
        }
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataDate.class);
    }

    @Override
    public DataColumn clone(String value) {
        DataDate dataDate = new DataDate(application, index, type, name, getValue(value));
        dataDate.setNullString(nullString);
        return dataDate;
    }

    @Override
    public DataColumn clone(int index, String name) {
        DataDate dataDate = new DataDate(application, index, type, name, value);
        dataDate.setNullString(nullString);
        return dataDate;
    }

    public Date getDateValue() {
        return value;
    }

    @Override
    public String getQuotedValue() {
        Date value = getDateValue();

        if (value == null) {
            return nullString;
        }

        String formatted = format(value, Defaults.DATE_FORMAT.getStringValue());
        if (formatted == null || formatted.equals(nullString)) {
            return nullString;
        }

        return quotes + formatted + quotes;
    }

    private Date getValue(String quotedValue) {
        if (quotedValue == null) {
            return null;
        }

        return parse(quotedValue, Defaults.DATE_FORMAT.getStringValue());
    }

    @Override
    public void setValue(String value) {
        this.value = parse(value, Defaults.DATE_FORMAT.getStringValue());
    }

    @Override
    public String getValue() {
        Date value = getDateValue();
        return format(value, Defaults.DATE_FORMAT.getStringValue());
    }

    @Override
    public String getFormattedValue(String format) {
        Date value = getDateValue();
        return format(value, format);
    }

    public void setValue(Date value) {
        this.value = value;
    }

    private String format(Date date, String format) {
        if (date == null) {
            return nullString;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        long year = Long.parseLong(simpleDateFormat.format(date));
        if (year > 9999) {
            return nullString;
        }

        try {
            simpleDateFormat = new SimpleDateFormat(format);
        } catch (Exception ex) {
            log.warn("DataDate.format, invalid date format({})", format);
            return nullString;
        }

        return simpleDateFormat.format(date);
    }

    private Date parse(String date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date parsed;

        try {
            parsed = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            log.warn("DataDate.parse(date:{}, format:{}). has exception: {}", date, format, e);
            parsed = null;
        }

        return parsed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("valid", valid)
                .append("index", index)
                .append("name", name)
                .append("type", type)
                .append("value", value)
                .toString();
    }
}
