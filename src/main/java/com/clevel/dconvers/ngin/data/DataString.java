package com.clevel.dconvers.ngin.data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DataString extends DataColumn {

    private String value;

    public DataString(int index, int type, String name, String value) {
        super(index, type, name);

        this.value = value;
    }

    @Override
    public DataColumn clone(int index, String name) {
        return new DataString(index, type, name, value);
    }

    @Override
    public String getQuotedValue() {
        if (value == null) {
            return "null";
        }

        return "'" + value + "'";
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
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
