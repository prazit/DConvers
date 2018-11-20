package com.clevel.dconvers.conf;

import java.sql.Types;

public enum SystemVariable {

    SOURCE_FILE_NUMBER(Types.INTEGER),
    TARGET_FILE_NUMBER(Types.INTEGER),
    MAPPING_FILE_NUMBER(Types.INTEGER),
    ROWNUMBER(Types.INTEGER),
    NOW(Types.TIMESTAMP),
    NULL(Types.VARCHAR)
    ;

    private int dataType;

    SystemVariable(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

    public static SystemVariable parse(String name) {
        SystemVariable systemVariable;

        try {
            systemVariable = SystemVariable.valueOf(name);
        } catch (IllegalArgumentException ex) {
            systemVariable = null;
        }

        return systemVariable;
    }
}
