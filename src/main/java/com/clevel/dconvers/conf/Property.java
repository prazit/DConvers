package com.clevel.dconvers.conf;

import org.slf4j.LoggerFactory;

public enum Property {

    PLUGINS("plugins"),
    CALCULATOR("calculator"),
    VARIABLE("variable"),

    DATA_SOURCE("datasource"),
    SFTP("sftp"),
    COMBINE("combine"),
    URL("url"),
    DRIVER("driver"),
    SCHEMA("schema"),
    USER("user"),
    PASSWORD("password"),
    RETRY("retry"),
    PROPERTIES("prop"),
    PRE("pre"),
    POST("post"),
    HOST("host"),
    PORT("port"),
    SSL("ssl"),

    CONVERTER_FILE("converter"),
    SOURCE_PATH("source.output"),
    TARGET_PATH("target.output"),
    MAPPING_PATH("mapping.output"),

    EXIT_ON_ERROR("exit.on.error"),
    EXIT_CODE_ERROR("exit.code.error"),
    EXIT_CODE_WARNING("exit.code.warning"),
    EXIT_CODE_SUCCESS("exit.code.success"),

    SOURCE("source"),
    SQL("sql"),
    EMAIL("email"),
    QUERY("query"),
    ID("id"),
    FOR("for"),

    TARGET("target"),
    MAPPING("mapping"),
    TRANSFER("transfer"),
    SUMMARY("summary"),
    CREATE("create"),
    INSERT("insert"),
    UPDATE("update"),
    MARKDOWN("markdown"),
    MERMAID("mermaid"),
    FULL("full"),
    PDF_TABLE("pdf"),
    TXT("txt"),
    CSV("csv"),
    SRC("src"),
    TAR("tar"),
    DBINSERT("dbinsert"),
    DBUPDATE("dbupdate"),
    DBEXECUTE("dbexecute"),
    OSVARIABLE("osvariable"),
    CONF("conf"),
    QUOTES("quotes"),
    NAME("name"),
    VALUE("value"),
    OUTPUT_FILE("output"),
    OUTPUT_TYPES("outputs"),
    OUTPUT_APPEND("append"),
    OUTPUT_AUTOCREATEDIR("auto.create.dir"),
    OUTPUT_CHARSET("charset"),
    OUTPUT_BOF("bof"),
    OUTPUT_EOL("eol"),
    OUTPUT_EOF("eof"),
    COMMENT("comment"),
    HEADER("header"),
    OWNER("owner"),
    TABLE("table"),
    DBMS("dbms"),
    COLUMN("column"),
    TYPE("type"),
    IS_KEY("iskey"),
    INDEX("index"),
    QUERY_SPLIT("query.split"),
    POST_SQL("post"),
    PRE_SQL("pre"),
    SEPARATOR("separator"),
    LENGTH_MODE("length.mode"),
    NULL("null"),
    FORMAT("format"),
    FORMAT_INTEGER("format.integer"),
    FORMAT_DECIMAL("format.decimal"),
    FORMAT_STRING("format.string"),
    FORMAT_DATE("format.date"),
    FORMAT_DATETIME("format.datetime"),
    FILL_STRING("fill.string"),
    FILL_NUMBER("fill.number"),
    FILL_DATE("fill.date"),
    TRANSFORM("transform"),
    ARGUMENTS("arguments"),
    REPLACE("replace"),
    CURRENT("current"),
    SYSTEM("system"),
    RESULT_SET_META_DATA("ResultSetMetaData"),
    DIR("dir"),
    LINES("lines"),

    ROW_NUMBER("rownumber"),
    FILE_NUMBER("filenumber"),

    MAPPING_PREFIX("mapping.table.prefix"),
    REPORT_TABLE("report.table"),

    SOURCE_ID("source_id"),
    TARGET_ID("target_id"),

    VERSION_PROPERTIES("version.properties"),
    CURRENT_VERSION("current.ver"),
    PROJECT_NAME("project.name"),
    VERSION_NAME("version.name"),
    VERSION_NUMBER("version.number"),
    REVISION_NUMBER("revision.number"),
    BUILD_NUMBER("build.number"),
    BUILD_DATE("build.date");

    private String key;

    Property(String key) {
        this.key = key;
    }

    public static Property parse(String name) {
        Property property;

        try {
            name = name.toUpperCase();
            property = Property.valueOf(name);
        } catch (IllegalArgumentException ex) {
            property = null;
            LoggerFactory.getLogger(Property.class).error("Property.parse(name:{}) is failed!", name, ex);
        }

        return property;
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
