package com.clevel.dconvers.conf;

public enum Property {

    DATA_SOURCE("datasource"),
    SFTP("sftp"),
    URL("url"),
    DRIVER("driver"),
    SCHEMA("schema"),
    USER("user"),
    PASSWORD("password"),
    PRE("pre"),
    POST("post"),
    HOST("host"),
    PORT("port"),
    SSL("ssl"),

    CONVERTER_FILE("converter"),
    REPORT_PATH("report.output"),
    SOURCE_PATH("source.output"),
    TARGET_PATH("target.output"),
    MAPPING_PATH("mapping.output"),

    PROGRESS_OUTPUT("progress.output"),
    EXIT_ON_ERROR("exit.on.error"),

    SOURCE("source"),
    SQL("sql"),
    EMAIL("email"),
    QUERY("query"),
    ID("id"),
    GENERATE_TARGET("gen"),

    TARGET("target"),
    CREATE("create"),
    INSERT("insert"),
    UPDATE("update"),
    MARKDOWN("markdown"),
    PDF_TABLE("pdf"),
    TXT("txt"),
    CSV("csv"),
    SRC("src"),
    TAR("tar"),
    DBINSERT("dbinsert"),
    DBUPDATE("dbupdate"),
    CONF("conf"),
    QUOTES("quotes"),
    NAME("name"),
    VALUE("value"),
    OUTPUT_FILE("output"),
    OUTPUT_TYPES("outputs"),
    OUTPUT_APPEND("append"),
    OUTPUT_AUTOCREATEDIR("auto.create.dir"),
    OUTPUT_CHARSET("charset"),
    OUTPUT_EOL("eol"),
    OUTPUT_EOF("eof"),
    TABLE("table"),
    COLUMN("column"),
    TYPE("type"),
    IS_KEY("iskey"),
    INDEX("index"),
    POST_SQL("post"),
    PRE_SQL("pre"),
    SEPARATOR("separator"),
    FORMAT("format"),
    FORMAT_DATE("format.date"),
    FORMAT_DATETIME("format.datetime"),
    FILL_STRING("fill.string"),
    FILL_NUMBER("fill.number"),
    FILL_DATE("fill.date"),
    TRANSFORM("transform"),
    ARGUMENTS("arguments"),
    REPLACE("replace"),
    CURRENT("current"),

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

    public String prefixKey(String baseProperty) {
        return baseProperty + "." + this.key;
    }

}
