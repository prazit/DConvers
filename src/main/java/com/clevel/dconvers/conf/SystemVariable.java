package com.clevel.dconvers.conf;

import org.slf4j.LoggerFactory;

import java.sql.Types;

public enum SystemVariable {

    SOURCE_OUTPUT_PATH(Types.VARCHAR),
    TARGET_OUTPUT_PATH(Types.VARCHAR),
    MAPPING_OUTPUT_PATH(Types.VARCHAR),

    SOURCE_FILE_NUMBER(Types.INTEGER),
    TARGET_FILE_NUMBER(Types.INTEGER),
    MAPPING_FILE_NUMBER(Types.INTEGER),

    // Row number will be reset at the beginning of any datatable processes.
    ROW_NUMBER(Types.INTEGER),

    // EXIT CODE for now
    APPLICATION_STATE(Types.INTEGER),

    // Constant values for configuration
    EMPTY_STRING(Types.VARCHAR),    // ""
    APPLICATION_START(Types.TIMESTAMP),     // The time to start this application.

    NOW(Types.TIMESTAMP)                   // Time in realtime.
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
            name = name.toUpperCase();
            systemVariable = SystemVariable.valueOf(name);
        } catch (IllegalArgumentException ex) {
            systemVariable = null;
            //LoggerFactory.getLogger(SystemVariable.class).error("SystemVariable.parse(name:{}) is failed! {}", name, ex.getMessage());
        }

        return systemVariable;
    }
}
