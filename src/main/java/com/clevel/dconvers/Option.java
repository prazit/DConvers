package com.clevel.dconvers;

public enum Option {

    SOURCE(true, "s", "source", true, "source file is required for data conversion, please see 'sample-source.conf' for detailed"),

    VERBOSE(false, "v", "verbose", false, "run in verbose mode will show TRACE level of messages"),
    LEVEL(false, "l", "level", true, "specified level will use as level of messages\npossible values are TRACE, DEBUG, ERROR, WARNING, INFO"),

    HELP(false, "h", "help", false, "print help message to console")
    ;

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
