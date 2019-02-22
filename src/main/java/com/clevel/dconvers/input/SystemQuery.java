package com.clevel.dconvers.input;

import org.slf4j.LoggerFactory;

public enum SystemQuery {

    ARG,
    VARIABLE,
    ENVIRONMENT,

    SUMMARY
    ;

    public static SystemQuery parse(String name) {
        SystemQuery systemQuery;

        try {
            name = name.toUpperCase();
            systemQuery = SystemQuery.valueOf(name);
        } catch (IllegalArgumentException ex) {
            systemQuery = null;
            LoggerFactory.getLogger(SystemQuery.class).error("SystemQuery.parse(name:{}) is failed!", name, ex);
        }

        return systemQuery;
    }
}
