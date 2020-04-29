package com.clevel.dconvers.data;

import com.clevel.dconvers.DConvers;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class DataLong extends DataColumn {
    private Long value;

    public DataLong(DConvers dconvers, int index, int type, String name, Long value) {
        super(dconvers, index, type, name);

        this.value = value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataLong.class);
    }

    @Override
    public DataColumn clone(String value) {
        DataLong dataLong = new DataLong(dconvers, index, type, name, Long.parseLong(value));
        dataLong.setNullString(nullString);
        return dataLong;
    }

    @Override
    public DataColumn clone(int index, String name) {
        DataLong dataLong = new DataLong(dconvers, index, type, name, value);
        dataLong.setNullString(nullString);
        return dataLong;
    }

    public Long getLongValue() {
        return value;
    }

    @Override
    public String getQuotedValue() {
        if (isNull()) {
            return nullString;
        }
        return String.valueOf(value);
    }

    @Override
    public String getValue() {
        if (isNull()) {
            return nullString;
        }
        return String.valueOf(value);
    }

    @Override
    public String getFormattedValue(String pattern) {
        if (isNull()) {
            return nullString;
        }

        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(value);
    }

    @Override
    public void setValue(String value) {
        if (NumberUtils.isCreatable(value)) {
            this.value = NumberUtils.createLong(value);
        } else {
            this.value = null;
            error("Integer value is required for setValue() instead of '{}'", value);
        }
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public boolean isNull() {
        return value == null;
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
