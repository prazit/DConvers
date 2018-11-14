package com.clevel.dconvers.ngin.data;

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
    private Logger log;

    public DataDate(int index, int type, String name, Date value) {
        super(index, type, name);
        log = LoggerFactory.getLogger(DataDate.class);
        this.value = value;
    }

    public DataDate(int index, int type, String name, String value) {
        super(index, type, name);
        log = LoggerFactory.getLogger(DataDate.class);

        if (value == null) {
            this.value = null;
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.applyPattern(Defaults.DATE_FORMAT.getStringValue());
            try {
                this.value = simpleDateFormat.parse(value);
            } catch (Exception e) {
                log.error("DataDate. parse date ({}) is failed.", value, e.getMessage());
                this.value = null;
            }
        }
    }

    @Override
    public DataColumn clone(String value) {
        DataDate dataDate = new DataDate(index, type, name, getValue(value));
        dataDate.setNullString(nullString);
        return dataDate;
    }

    @Override
    public DataColumn clone(int index, String name) {
        DataDate dataDate = new DataDate(index, type, name, value);
        dataDate.setNullString(nullString);
        return dataDate;
    }

    public Date getDateValue() {
        return value;
    }

    @Override
    public String getQuotedValue() {
        if (value == null) {
            return nullString;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("YYYY");
        long year = Long.parseLong(simpleDateFormat.format(value));
        if (year > 9999) {
            return nullString;
        }

        simpleDateFormat.applyPattern(Defaults.DATE_FORMAT.getStringValue());
        return "\"" + simpleDateFormat.format(value) + "\"";
    }

    private Date getValue(String quotedValue) {
        if (quotedValue == null) {
            return null;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern(Defaults.DATE_FORMAT.getStringValue());

        try {
            return simpleDateFormat.parse(quotedValue);
        } catch (ParseException e) {
            log.warn("DataDate.getValue. has exception: {}", e);
            return null;
        }
    }

    @Override
    public void setValue(String value) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern(Defaults.DATE_FORMAT.getStringValue());

        try {
            this.value = simpleDateFormat.parse(value);
        } catch (ParseException e) {
            log.warn("DataDate.getValue. has exception: {}", e);
            this.value = null;
        }

    }

    @Override
    public String getValue() {
        return getFormattedValue(Defaults.DATE_FORMAT.getStringValue());
    }

    public String getFormattedValue(String format) {
        if (value == null) {
            return nullString;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("YYYY");
        long year = Long.parseLong(simpleDateFormat.format(value));
        if (year > 9999) {
            return nullString;
        }

        simpleDateFormat.applyPattern(format);
        return simpleDateFormat.format(value);
    }

    public void setValue(Date value) {
        this.value = value;
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
