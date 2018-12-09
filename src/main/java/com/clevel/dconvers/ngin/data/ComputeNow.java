package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.Application;

import java.sql.Types;
import java.util.Date;

public class ComputeNow extends DataDate {

    public ComputeNow(Application application, String name) {
        super(application, 0, Types.DATE, name, new Date());
    }

    @Override
    public Date getDateValue() {
        return new Date();
    }
}
