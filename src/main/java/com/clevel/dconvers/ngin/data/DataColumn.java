package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.ngin.ValidatorBase;

import java.sql.Types;

public abstract class DataColumn extends ValidatorBase {

    protected String name;
    protected int index;
    protected int type;

    DataColumn(int index, int type, String name) {
        this.index = index;
        this.type = type;
        this.name = name;
        valid = (index >= 0) && (name != null);
    }

    public int getIndex() {
        return index;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public abstract String getQuotedValue();

    public abstract String getValue();

    public abstract DataColumn clone(String value);

    public abstract DataColumn clone(int index, String name);
}
