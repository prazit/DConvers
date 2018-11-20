package com.clevel.dconvers.conf;

import java.sql.Types;

public enum DynamicValueType {
    VAR(0),                 // VAR:system-variable-name
    CAL(0),                 // CAL:calculator(argument1,argument2,...)

    STR(Types.VARCHAR),     // STR:My string is longer than other value
    INT(Types.INTEGER),     // INT:integer-value = INT:12345
    DEC(Types.DECIMAL),     // DEC:decimal-value = DEC:12345.54321
    DTE(Types.DATE),        // DTE:DD/MM/YYYY = DTE:31/12/2020
    DTT(Types.TIMESTAMP),   // DTT:DD/MM/YYYY hh:mm:ss => DTT:31/12/2020 23:59:59

    SRC(0),                 // SRC:source-name.column-name  // the target mapping file need to define as a source with the standard mapping table name such as 'map_branch_to_branch_address'
    TAR(0),                 // TAR:target-name.column-name  // the target 'target-name' must be use the same source with this target, because of dconvers will use mapping table between TAR:target-name and target.target-name.source
    MAP(0),                 // MAP:mapping-name.column-name
    TXT(Types.VARCHAR),     // TXT:full-path\text-file.ext

    COL(0),                 // source-column-name>>MAPPING>>column-name = id>>MAP:branch_to_branch.source_id>>target_id
    NON(0),                 // None (no specified type) will use as source-column-name
    INV(0)                  // Invalid Specified Type
    ;

    private int dataType;

    DynamicValueType(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

    public String getValuePrefix() {
        return name() + ":";
    }

    public static DynamicValueType parse(String name) {
        DynamicValueType dynamicValueType;

        try {
            dynamicValueType = DynamicValueType.valueOf(name);
        } catch (IllegalArgumentException ex) {
            dynamicValueType = null;
        }

        return dynamicValueType;
    }
}
