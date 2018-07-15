package com.clevel.dconvers.ngin;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DataLong extends DataColumn {
    private long value;

    public DataLong(int index, int type, String name, long value) {
        super(index, type, name);

        this.value = value;
    }

    @Override
    public DataColumn clone() {
        return new DataLong(index, type, name, value);
    }

    @Override
    public String getValue() {
        return String.valueOf(value);
    }

    public void setValue(long value) {
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
