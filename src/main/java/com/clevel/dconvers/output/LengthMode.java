package com.clevel.dconvers.output;

import org.slf4j.LoggerFactory;

public enum LengthMode {
    BYTE,
    CHAR;

    public static LengthMode parse(String name) {
        LengthMode lengthMode;

        try {
            name = name.toUpperCase();
            lengthMode = LengthMode.valueOf(name);
        } catch (IllegalArgumentException ex) {
            lengthMode = null;
            LoggerFactory.getLogger(LengthMode.class).error("LengthMode.parse(name:{}) is failed! {}", name, ex.getMessage());
        }

        return lengthMode;
    }
}
