package com.clevel.dconvers.dynvalue;

import org.slf4j.LoggerFactory;

public enum TableReaderMarker {

    TAB,
    COL,
    ;

    public static TableReaderMarker parse(String name) {
        TableReaderMarker tableReaderMarker;

        try {
            name = name.toUpperCase();
            tableReaderMarker = TableReaderMarker.valueOf(name);
        } catch (Exception ex) {
            tableReaderMarker = null;
            LoggerFactory.getLogger(TableReaderMarker.class).error("TableReaderMarker.parse(name:{}) is failed!", name, ex);
        }

        return tableReaderMarker;
    }

}
