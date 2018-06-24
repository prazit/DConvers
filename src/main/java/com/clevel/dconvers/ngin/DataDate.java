package com.clevel.dconvers.ngin;

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
}
