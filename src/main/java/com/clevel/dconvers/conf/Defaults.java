package com.clevel.dconvers.conf;

import java.sql.Types;

public enum Defaults {

    CONFIG_FILE_EXT(".conf"),
    VERBOSE_LOG_LEVEL("TRACE"),
    NORMAL_LOG_LEVEL("INFO"),

    PROGRESS_SHOW_KILO_AFTER(4000),
    PROGRESS_UPDATE_INTERVAL_MILLISEC(100),

    DATE_FORMAT("yyyy/MM/dd HH:mm:ss"),
    NUMBER_FORMAT("#,##0"),
    DECIMAL_FORMAT("#,##0.00"),

    EXIT_CODE_SUCCESS(0),
    EXIT_CODE_ERROR(1),
    EXIT_CODE_WARNING(2)
    ;

    private int type;

    private String stringValue;
    private long longValue;

    Defaults(String stringValue) {
        this.stringValue = stringValue;
        type = Types.VARCHAR;
    }

    Defaults(long longValue) {
        this.longValue = longValue;
        type = Types.INTEGER;
    }

    public String getStringValue() {
        if (type == Types.INTEGER) {
            return String.valueOf(longValue);
        }

        return stringValue;
    }

    public long getLongValue() {
        if (type == Types.VARCHAR) {
            return Long.parseLong(stringValue);
        }

        return longValue;
    }

    public int getIntValue() {
        if (type == Types.VARCHAR) {
            return Integer.parseInt(stringValue);
        }

        return (int) longValue;
    }
}