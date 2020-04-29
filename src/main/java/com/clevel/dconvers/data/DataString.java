package com.clevel.dconvers.data;

import com.clevel.dconvers.DConvers;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataString extends DataColumn {

    private String value;

    public DataString(DConvers dconvers, int index, int type, String name, String value) {
        super(dconvers, index, type, name);

        this.value = value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataString.class);
    }

    @Override
    public DataColumn clone(String value) {
        DataString dataString = new DataString(dconvers, index, type, name, value);
        dataString.setNullString(nullString);
        return dataString;
    }

    @Override
    public DataColumn clone(int index, String name) {
        DataString dataString = new DataString(dconvers, index, type, name, value);
        dataString.setNullString(nullString);
        return dataString;
    }

    @Override
    public String getQuotedValue() {
        if (isNull()) {
            return nullString;
        }

        if (value.equals(nullString)) {
            return nullString;
        }

        return quotes + escape(value, quotes) + quotes;
    }

    private String escape(String quotedValue, String quoteSymbol) {
        String value = quotedValue.replaceAll("\r\n|\n\r|\n", "\\\\n");
        value = value.replaceAll("[" + quoteSymbol + "]", "\\\\" + quoteSymbol);
        return value;
    }

    @Override
    public String getValue() {
        if (isNull()) {
            return nullString;
        }

        return value;
    }

    @Override
    public String getFormattedValue(String pattern) {
        return getValue();
    }

    public void appendValue(String value) {
        if (this.value == null) {
            this.value = value;
        } else {
            this.value += value;
        }
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean isNull() {
        return value == null;
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
