package com.clevel.dconvers.conf;

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

    // Version of current configuration
    CONFIG_VERSION(Types.VARCHAR),

    // Current Version of DConvers.jar (full text)
    APPLICATION_FULL_VERSION(Types.VARCHAR),

    // Current Version of DConvers.jar
    APPLICATION_VERSION(Types.VARCHAR),

    // EXIT CODE for now
    APPLICATION_STATE(Types.INTEGER),
    APPLICATION_STATE_MESSAGE(Types.VARCHAR),

    // Constant values for configuration
    EMPTY_STRING(Types.VARCHAR),    // ""
    APPLICATION_START(Types.TIMESTAMP),     // The time to start this application.

    NOW(Types.TIMESTAMP),                   // Time in realtime.

    // Variables for Table Reader
    TABLE_READER(Types.VARCHAR),
    ROW_READER(Types.INTEGER),
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
