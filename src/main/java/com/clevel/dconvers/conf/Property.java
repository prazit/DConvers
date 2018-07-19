package com.clevel.dconvers.conf;

public enum Property {

    DATA_SOURCE("datasource"),
        URL("url"),
        DRIVER("driver"),
        SCHEMA("schema"),
        USER("user"),
        PASSWORD("password"),

    CONVERTER_FILE("converter"),
        REPORT_PATH("output.report"),
        SOURCE_PATH("output.source"),
        TARGET_PATH("output.target"),
        MAPPING_PATH("output.mapping"),

    SOURCE("source"),
        QUERY("query"),
        ID("id"),
        GENERATE_TARGET("gen"),

    TARGET("target"),
        CREATE("create"),
        INSERT("insert"),
        OUTPUT_FILE("output"),
        TABLE("table"),
        COLUMN("column"),

    ROW_NUMBER("rownumber"),
    FILE_NUMBER("filenumber"),

    MAPPING_PREFIX("mapping.table.prefix"),
    REPORT_TABLE("report.table"),

    SOURCE_ID("source_id"),
    TARGET_ID("target_id");

    private String key;

    Property(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public String connectKey(String name) {
        return this.key() +"."+ name;
    }

    public String connectKey(String name, Property property) {
        return this.key() +"."+ name +"."+ property.key();
    }

    public String connectKey(Property property) {
        return this.key() +"."+ property.key();
    }
}
