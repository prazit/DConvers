package com.clevel.dconvers.conf;

public enum Defaults {

    CONFIG_FILE_EXT(".conf"),
    VERBOSE_LOG_LEVEL("TRACE"),
    NORMAL_LOG_LEVEL("INFO")
    ;

    private String defaultValue;

    Defaults(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
