package com.clevel.dconvers.conf;

public enum Property {

    DATA_SOURCE("datasource"),
        URL("url"),
        DRIVER("driver"),
        SCHEMA("schema"),
        USER("user"),
        PASSWORD("password"),

    CONVERTER_FILE("converter"),
        OUTPUT_PATH("output"),

    SOURCE("source"),
        QUERY("query"),

    TARGET("target"),
        CREATE("create"),
        INSERT("insert"),
        OUTPUT_FILE("output"),
        TABLE("table"),
        COLUMN("column")

    ;

    private String key;

    Property(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public String connectKey(String name, Property property) {
        return this.key() +"."+ name +"."+ property.key();
    }
}
