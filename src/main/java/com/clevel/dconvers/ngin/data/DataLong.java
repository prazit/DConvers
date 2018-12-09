package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.Application;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class DataLong extends DataColumn {
    private Long value;

    public DataLong(Application application, int index, int type, String name, Long value) {
        super(application, index, type, name);

        this.value = value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataLong.class);
    }

    @Override
    public DataColumn clone(String value) {
        DataLong dataLong = new DataLong(application, index, type, name, Long.parseLong(value));
        dataLong.setNullString(nullString);
        return dataLong;
    }

    @Override
    public DataColumn clone(int index, String name) {
        DataLong dataLong = new DataLong(application, index, type, name, value);
        dataLong.setNullString(nullString);
        return dataLong;
    }

    public Long getLongValue() {
        return value;
    }

    @Override
    public String getQuotedValue() {
        if (value == null) {
            return nullString;
        }
        return String.valueOf(value);
    }

    @Override
    public String getValue() {
        if (value == null) {
            return nullString;
        }
        return String.valueOf(value);
    }

    @Override
    public String getFormattedValue(String pattern) {
        if (value == null) {
            return nullString;
        }

        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(value);
    }

    @Override
    public void setValue(String value) {
        this.value = Long.parseLong(value);
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public void increaseValueBy(long increase) {
        this.value += increase;
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
