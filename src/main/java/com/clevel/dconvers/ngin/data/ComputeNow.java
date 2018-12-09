package com.clevel.dconvers.ngin.data;

import java.sql.Types;
import java.util.Date;

public class ComputeNow extends DataDate {

    public ComputeNow(String name) {
        super(0, Types.DATE, name, new Date());
    }

    @Override
    public Date getDateValue() {
        return new Date();
    }
}
