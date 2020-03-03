package com.clevel.dconvers.ngin;

public class PropertyValue extends ValidatorBase {
    String name;
    String separator;
    String value;

    public PropertyValue(String httpLine, String separator) {
        this.separator = separator;

        int separatorIndex = httpLine.indexOf(separator, 1);
        if (separatorIndex < 0) {
            name = null;
            value = null;
            valid = false;
        }

        name = httpLine.substring(0, separatorIndex);
        value = httpLine.substring(separatorIndex + 1);
        valid = true;
    }

}
