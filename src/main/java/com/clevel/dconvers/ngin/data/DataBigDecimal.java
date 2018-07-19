package com.clevel.dconvers.ngin.data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

public class DataBigDecimal extends DataColumn {

    private BigDecimal value;

    public DataBigDecimal(int index, int type, String name, BigDecimal value) {
        super(index, type, name);

        this.value = value;
    }

    @Override
    public DataColumn clone(int index, String name) {
        return new DataBigDecimal(index, type, name, value);
    }

    @Override
    public String getQuotedValue() {
        if (value == null) {
            return "null";
        }

        return value.toString();
    }

    @Override
    public String getValue() {
        return value.toString();
    }

    public void setValue(BigDecimal value) {
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
