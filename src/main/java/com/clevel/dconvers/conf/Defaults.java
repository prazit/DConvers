package com.clevel.dconvers.conf;

public enum Defaults {

    CONFIG_FILE_EXT(".conf"),
    VERBOSE_LEVEL("TRACE")
    ;

    private String defaultValue;

    Defaults(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
