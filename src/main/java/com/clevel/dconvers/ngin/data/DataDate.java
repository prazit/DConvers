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
    public String getQuotedValue() {
        if (value == null) {
            return "null";
        }

        // TODO Invalid Year made sql error like this > Data truncation: Incorrect datetime value: '10000/12/31 00:00:00' for column 'end_date' at row 1

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("YYYY/MM/dd HH:mm:ss");
        return "'" + simpleDateFormat.format(value) + "'";
    }

    @Override
    public String getValue() {
        return value.toString();
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
