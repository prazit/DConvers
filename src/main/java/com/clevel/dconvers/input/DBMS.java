package com.clevel.dconvers.input;

import org.slf4j.LoggerFactory;

public enum DBMS {
    AS400,
    DB2,
    MYSQL,
    MARIADB,
    ORACLE,
    SQLSERVER;

    public static DBMS parse(String name) {
        DBMS dbms;

        try {
            name = name.toUpperCase();
            dbms = DBMS.valueOf(name);
        } catch (IllegalArgumentException ex) {
            dbms = null;
            LoggerFactory.getLogger(DBMS.class).error("DBMS.parse(name:{}) is failed!", name, ex);
        }

        return dbms;
    }
}
