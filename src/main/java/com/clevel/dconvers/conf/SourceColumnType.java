package com.clevel.dconvers.conf;

import java.sql.Types;

public enum SourceColumnType {
    VAR(0),                 // VAR:systemvariable
    STR(Types.VARCHAR),     // STR:My string is longer than other value
    INT(Types.INTEGER),     // INT:12345
    DEC(Types.DECIMAL),     // DEC:12345.54321
    DTE(Types.DATE),        // DTE:DD/MM/YYYY => DTE:31/12/2020
    DTT(Types.TIMESTAMP),   // DTE:DD/MM/YYYY hh:mm:ss => DTT:31/12/2020 23:59:59
    SRC(0),                 // SRC:Source_Column_Name
    NON(0),                 // None (no specified type)
    INV(0)                  // Invalid Specified Type
    ;

    private int dataType;

    SourceColumnType(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

    public String getValuePrefix() {
        return name() + ":";
    }
}
