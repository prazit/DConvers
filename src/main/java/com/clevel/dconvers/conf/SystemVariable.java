package com.clevel.dconvers.conf;

import java.sql.Types;

public enum SystemVariable {

    FILENUMBER(Types.INTEGER),
    ROWNUMBER(Types.INTEGER),
    NOW(Types.TIMESTAMP)
    ;

    private int dataType;

    SystemVariable(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }
}
