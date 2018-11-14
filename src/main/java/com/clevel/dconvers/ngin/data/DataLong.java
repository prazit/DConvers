package com.clevel.dconvers.ngin.data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DataLong extends DataColumn {
    private Long value;

    public DataLong(int index, int type, String name, Long value) {
        super(index, type, name);

        this.value = value;
    }

    @Override
    public DataColumn clone(String value) {
        DataLong dataLong = new DataLong(index, type, name, Long.parseLong(value));
        dataLong.setNullString(nullString);
        return dataLong;
    }

    @Override
    public DataColumn clone(int index, String name) {
        DataLong dataLong = new DataLong(index, type, name, value);
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
