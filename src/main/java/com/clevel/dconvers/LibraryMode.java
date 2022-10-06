package com.clevel.dconvers;

public enum LibraryMode {

    MANUAL,
    NORMAL,
    PRESET
    ;

    public static LibraryMode parse(String mode) {
        LibraryMode libraryMode;
        try {
            libraryMode = LibraryMode.valueOf(mode.toUpperCase());
        } catch (Exception ex) {
            libraryMode = null;
        }
        return libraryMode;
    }
}
