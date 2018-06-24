package com.clevel.dconvers.ngin;

import java.lang.reflect.Type;
import java.sql.Types;

public abstract class DataColumn extends ValidatorBase {

    private String name;
    private int index;
    private int type;

    DataColumn() {
        this.index = -1;
        this.type = Types.VARCHAR;
        this.name = "";
        valid = false;
    }

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

    public abstract String getValue();
}
