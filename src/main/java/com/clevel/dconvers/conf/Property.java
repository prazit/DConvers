package com.clevel.dconvers.conf;

public enum Property {

    DATA_SOURCE("datasource"),
    URL("url"),
    DRIVER("driver"),
    SCHEMA("schema"),
    USER("user"),
    PASSWORD("password"),
    PRE("pre"),
    POST("post"),

    CONVERTER_FILE("converter"),
    REPORT_PATH("report.output"),
    SOURCE_PATH("source.output"),
    TARGET_PATH("target.output"),
    MAPPING_PATH("mapping.output"),

    SOURCE("source"),
    SQL("sql"),
    EMAIL("email"),
    QUERY("query"),
    ID("id"),
    GENERATE_TARGET("gen"),

    TARGET("target"),
    CREATE("create"),
    INSERT("insert"),
    MARKDOWN("markdown"),
    OUTPUT_FILE("output"),
    TABLE("table"),
    COLUMN("column"),
    INDEX("index"),
    POST_UPDATE("post.update"),

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
        return this.key() + "." + name;
    }

    public String connectKey(String name, Property property) {
        return this.key() + "." + name + "." + property.key();
    }

    public String connectKey(Property property) {
        return this.key() + "." + property.key();
    }
}
