package com.clevel.dconvers;

public enum Option {

    SOURCE(false, "s", "source", true, "source file is required for data conversion, please see 'sample-conversion.conf' for detailed"),
    TEST(false, "t", "test", false, "force to ignore all tables, but allow other component to loading to test connection and configuration"),
    LIBRARY(false, "m", "library-mode", true, "library modes need to manual call the start function yourself\n possible <arg> are MANUAL, NORMAL, PRESET\nmanual = not load configs but can manual set directly for saveProperties()\nnormal = load configs from source file\npreset = load configs from dataConversionConfigFile.getProperties()"),
    SAVE_DEFAULT_VALUE(false, "save", "save-default-value", false, "in library-manual-mode, when call saveProperties will not save property with default values by default\nuse this switch if want to save the property with default value"),

    SOURCE_TYPE(false, "st", "source-type", true, "type of source file, default=properties\npossible <arg> are XML, JSON, PROPERTIES"),
    ARG(false, "r", "arg", true, "values in CSV format for the CAL:ARG()"),

    LOGBACK(false, "b", "logback", true, "full path to logback.xml"),
    VERBOSE(false, "v", "verbose", false, "run in verbose mode will show TRACE level of messages"),
    LEVEL(false, "l", "level", true, "specified level will use as level of messages\npossible <arg> are TRACE, DEBUG, ERROR, WARNING, INFO"),

    VERSION(false, "n", "version", false, "print version information to console"),
    HELP(false, "h", "help", false, "print help message to console");


    private String shortOpt;
    private String longOpt;
    private String description;
    private boolean hasArgument;
    private boolean required;

    Option(boolean required, String shortOpt, String longOpt, boolean hasArgument, String description) {
        this.required = required;
        this.shortOpt = shortOpt;
        this.longOpt = longOpt;
        this.description = description;
        this.hasArgument = hasArgument;
    }

    public String getShortOpt() {
        return shortOpt;
    }

    public String getLongOpt() {
        return longOpt;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHasArgument() {
        return hasArgument;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        return shortOpt;
    }
}
