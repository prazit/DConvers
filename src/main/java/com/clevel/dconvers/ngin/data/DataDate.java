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

    @Override
    public DataColumn clone(String value) {
        return new DataDate(index, type, name, getValue(value));
    }

    @Override
    public DataColumn clone(int index, String name) {
        return new DataDate(index, type, name, value);
    }

    @Override
    public String getQuotedValue() {
        if (value == null) {
            return "null";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("YYYY");
        long year = Long.parseLong(simpleDateFormat.format(value));
        if (year > 9999) {
            return "null";
        }

        simpleDateFormat.applyPattern(Defaults.DATE_FORMAT.getStringValue());
        return "'" + simpleDateFormat.format(value) + "'";
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
    public String getValue() {
        return value.toString();
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
