package com.clevel.dconvers.data;

import com.clevel.dconvers.DConvers;

import java.sql.Types;
import java.util.Date;

public class ComputeNow extends DataDate {

    public ComputeNow(DConvers dconvers, String name) {
        super(dconvers, 0, Types.DATE, name, new Date());
    }

    @Override
    public Date getDateValue() {
        return new Date();
    }
}
