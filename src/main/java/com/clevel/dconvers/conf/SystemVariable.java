package com.clevel.dconvers.conf;

import java.sql.Types;

public enum SystemVariable {

    SOURCE_FILE_NUMBER(Types.INTEGER),
    TARGET_FILE_NUMBER(Types.INTEGER),
    MAPPING_FILE_NUMBER(Types.INTEGER),

    // Reset every time to start the process of datatable.
    ROW_NUMBER(Types.INTEGER),

    // System Messages to see detail of status
    ERROR_MESSAGES(Types.VARCHAR),
    WARNING_MESSAGES(Types.VARCHAR),
    PROGRESS_MESSAGES(Types.VARCHAR),

    // EXIT CODE for now
    CURRENT_STATE(Types.INTEGER),

    // Constant values for configuration
    EMPTY_STRING(Types.VARCHAR),    // ""
    APPLICATION_START(Types.TIMESTAMP),     // The time to start this application.

    NOW(Types.TIMESTAMP),                   // Time in realtime.
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
            systemVariable = SystemVariable.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            systemVariable = null;
        }

        return systemVariable;
    }
}
