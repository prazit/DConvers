package com.clevel.dconvers.conf;

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
            dbms = DBMS.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            dbms = null;
        }

        return dbms;
    }
}
