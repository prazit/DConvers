package com.clevel.dconvers.conf;

public enum SystemQuery {

    ARG,
    VARIABLE,
    ENVIRONMENT;

    public static SystemQuery parse(String name) {
        SystemQuery systemQuery;

        try {
            systemQuery = SystemQuery.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            systemQuery = null;
        }

        return systemQuery;
    }
}
