package com.clevel.dconvers.ngin;

public class DataString extends DataColumn {

    private String value;

    DataString(int index, int type, String name, String value) {
        super(index, type, name);

        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
