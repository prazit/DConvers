package com.clevel.dconvers.ngin;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.sql.Date;

public class DataDate extends DataColumn {
    private Date value;

    DataDate(int index, int type, String name, Date value) {
        super(index, type, name);

        this.value = value;
    }

    @Override
    public String getValue() {
        return value.toString();
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
