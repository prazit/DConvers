package com.clevel.dconvers;

public enum ConfigFileTypes {

    PROPERTIES,
    XML,
    JSON,
    ;

    public static ConfigFileTypes parse(String sourceType) {
        ConfigFileTypes configFileTypes;
        try {
            configFileTypes = ConfigFileTypes.valueOf(sourceType.toUpperCase());
        }catch (Exception ex) {
            configFileTypes = PROPERTIES;
        }
        return configFileTypes;
    }
}
