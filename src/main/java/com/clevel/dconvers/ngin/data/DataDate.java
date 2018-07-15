package com.clevel.dconvers.ngin.data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public DataColumn clone(int index, String name) {
        return new DataDate(index, type, name, value);
    }

    @Override
    public String getValue() {
        if (value == null) {
            return "null";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("YYYY/MM/dd hh:mm:ss");
        return "'" + simpleDateFormat.format(value) + "'";
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
