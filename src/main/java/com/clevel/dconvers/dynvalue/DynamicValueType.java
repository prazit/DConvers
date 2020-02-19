package com.clevel.dconvers.dynvalue;

import org.slf4j.LoggerFactory;

import java.sql.Types;

public enum DynamicValueType {

    ARG(Types.VARCHAR, ARGValue.class),     // read application arguments

    STR(Types.VARCHAR, STRValue.class),     // STR:My string is longer than other value
    INT(Types.INTEGER, INTValue.class),     // INT:integer-value = INT:12345
    DEC(Types.DECIMAL, DECValue.class),     // DEC:decimal-value = DEC:12345.54321
    DTE(Types.DATE, DTEValue.class),        // DTE:DD/MM/yyyy = DTE:31/12/2020
    DTT(Types.DATE, DTEValue.class),        // DTT:DD/MM/yyyy hh:mm:ss => DTT:31/12/2020 23:59:59

    SYS(0, SRCValue.class),        // SYS:source-name.column-name  // the target mapping file need to define as a source with the standard mapping table name such as 'map_branch_to_branch_address'
    SRC(0, SRCValue.class),        // SRC:source-name.column-name  // the target mapping file need to define as a source with the standard mapping table name such as 'map_branch_to_branch_address'
    TAR(0, SRCValue.class),        // TAR:target-name.column-name  // the target 'target-name' must be use the same source with this target, because of dconvers will use mapping table between TAR:target-name and target.target-name.source
    MAP(0, SRCValue.class),        // MAP:mapping-name.column-name

    TXT(Types.VARCHAR, TXTValue.class),     // TXT:full-path\text-file.ext
    HTP(Types.VARCHAR, HTTPValue.class),    // HTP:full-path\text-file.http
    FTP(Types.VARCHAR, FTPValue.class),     // FTP:<sftp-server-name>/full-path/text-file.ext

    COL(0, COLValue.class),        // source-column-name>>MAPPING>>column-name = id>>MAP:branch_to_branch.source_id>>target_id
    NON(0, NONValue.class),        // None (no specified type) will use as source-column-name
    INV(0, INVValue.class),        // Invalid Specified Type

    VAR(0, VARValue.class),        // VAR:system-variable-name
    CAL(0, CALValue.class),        // CAL:calculator(argument1,argument2,)
    ;

    private int dataType;
    private Class dynamicValueClass;

    DynamicValueType(int dataType, Class dynamicValueClass) {
        this.dataType = dataType;
        this.dynamicValueClass = dynamicValueClass;
    }

    public int getDataType() {
        return dataType;
    }

    public Class getDynamicValueClass() {
        return dynamicValueClass;
    }

    public static DynamicValueType parse(String name) {
        DynamicValueType dynamicValueType;

        try {
            name = name.toUpperCase();
            dynamicValueType = DynamicValueType.valueOf(name);
        } catch (Exception ex) {
            dynamicValueType = null;
            LoggerFactory.getLogger(DynamicValueType.class).error("DynamicValueType.parse(name:{}) is failed!", name, ex);
        }

        return dynamicValueType;
    }

    public static DynamicValueType parseTargetColumn(String columnValue) {
        if (columnValue.length() < 5) {
            return DynamicValueType.NON;
        }

        if (columnValue.indexOf(">>") >= 0) {
            return DynamicValueType.COL;
        }

        char ch = columnValue.charAt(3);
        if (ch != ':') {
            return DynamicValueType.NON;
        }

        String keyWord = columnValue.substring(0, 3).toUpperCase();
        DynamicValueType dynamicValueType;
        try {
            dynamicValueType = DynamicValueType.parse(keyWord);
        } catch (IllegalArgumentException e) {
            return DynamicValueType.INV;
        }

        return dynamicValueType;
    }
}
