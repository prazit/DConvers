package com.clevel.dconvers.ngin;

import java.math.BigDecimal;

public class DataBigDecimal extends DataColumn {

    private BigDecimal value;

    DataBigDecimal(int index, int type, String name, BigDecimal value) {
        super(index, type, name);

        this.value = value;
    }

    @Override
    public String getValue() {
        return value.toString();
    }
}
