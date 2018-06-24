package com.clevel.dconvers.ngin;

public class DataLong extends DataColumn {
    private long value;

    DataLong(int index, int type, String name, long value) {
        super(index, type, name);

        this.value = value;
    }

    @Override
    public String getValue() {
        return String.valueOf(value);
    }
}
