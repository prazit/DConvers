package com.clevel.dconvers.conf;

import org.slf4j.LoggerFactory;

public enum SystemQuery {

    ARG,
    VARIABLE,
    ENVIRONMENT;

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
