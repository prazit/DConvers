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
    public DataColumn clone(String value) {
        DataString dataString = new DataString(index, type, name, value);
        dataString.setNullString(nullString);
        return dataString;
    }

    @Override
    public DataColumn clone(int index, String name) {
        DataString dataString = new DataString(index, type, name, value);
        dataString.setNullString(nullString);
        return dataString;
    }

    @Override
    public String getQuotedValue() {
        if (value == null) {
            return nullString;
        }

        return quotes + value + quotes;
    }

    @Override
    public String getValue() {
        if (value == null) {
            return nullString;
        }

        return value;
    }

    @Override
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
