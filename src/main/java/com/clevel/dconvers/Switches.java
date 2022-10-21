package com.clevel.dconvers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.ngin.AppBase;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Switches extends AppBase {

    private CommandLine cmd;

    private Options options;

    private String source;
    private String logback;
    private String arg;
    private ConfigFileTypes sourceType;
    private boolean verbose;
    private Level verboseLevel;
    private boolean test;
    private boolean help;
    private boolean version;

    private boolean library;
    private LibraryMode libraryMode;
    private boolean saveDefaultValue;
    private String[] args;

    public Switches(String[] args) {
        super(null, "switches");

        this.args = args;
        loadLogger();

        options = new Options();
        registerSwitchesByOptions();

        log.debug("Switches is created.");
    }

    public void postConstruct() {
        valid = loadSwitches();
        if (valid) {
            valid = validate();
        }
    }

    private void registerSwitchesByOptions() {
        for (Option option : Option.values()) {
            if (option.isRequired()) {
                options.addRequiredOption(option.getShortOpt(), option.getLongOpt(), option.isHasArgument(), option.getDescription());
            } else {
                options.addOption(option.getShortOpt(), option.getLongOpt(), option.isHasArgument(), option.getDescription());
            }
        }
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Switches.class);
    }

    private boolean loadSwitches() {
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            error("parse switches failed: " + e.getMessage());
            return false;
        }

        String level = cmd.getOptionValue(Option.LEVEL.getShortOpt());
        verbose = cmd.hasOption(Option.VERBOSE.getShortOpt());
        if (level == null) level = (verbose) ? Defaults.VERBOSE_LOG_LEVEL.getStringValue() : Defaults.NORMAL_LOG_LEVEL.getStringValue();

        try {
            verboseLevel = Level.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException ex) {
            error("parse verbose level failed: " + ex.getMessage());
            return false;
        }


        String libMode = cmd.getOptionValue(Option.LIBRARY.getShortOpt());
        if (libMode != null) {
            library = true;
            libraryMode = LibraryMode.parse(libMode);
        }
        saveDefaultValue = cmd.hasOption(Option.SAVE_DEFAULT_VALUE.getLongOpt());

        if (!library) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
            loggerList.forEach(tmpLogger -> tmpLogger.setLevel(verboseLevel));
            log.debug("Switches.loadSwitches.");
            log.debug("verbose level is {}", verboseLevel.toString());
        }

        source = cmd.getOptionValue(Option.SOURCE.getShortOpt());
        if (source != null && source.lastIndexOf(".") < 0) {
            source = source + Defaults.CONFIG_FILE_EXT.getStringValue();
        }
        sourceType = ConfigFileTypes.parse(cmd.getOptionValue(Option.SOURCE_TYPE.getShortOpt()));

        test = cmd.hasOption(Option.TEST.getShortOpt());
        help = cmd.hasOption(Option.HELP.getShortOpt());
        version = cmd.hasOption(Option.VERSION.getShortOpt());
        arg = cmd.getOptionValue(Option.ARG.getShortOpt());

        return true;
    }

    @Override
    public boolean validate() {
        log.debug("Switches.validateSwitches.");

        if (source == null && !isLibrary() && !help && !version) {
            error("invalid specified source-file");
            return false;
        }

        return true;
    }

    public String getSource() {
        return source;
    }

    public ConfigFileTypes getSourceType() {
        return sourceType;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public Level getVerboseLevel() {
        return verboseLevel;
    }

    public Options getOptions() {
        return options;
    }

    public boolean isTest() {
        return test;
    }

    public boolean isHelp() {
        return help;
    }

    public boolean isVersion() {
        return version;
    }

    public String[] getArgs() {
        return args;
    }

    public String getArg() {
        return arg;
    }

    public boolean isLibrary() {
        return library;
    }

    public LibraryMode getLibraryMode() {
        return libraryMode;
    }

    public boolean isSaveDefaultValue() {
        return saveDefaultValue;
    }

    @Override
    public void error(String msg) {
        log.error("Switches : " + msg);
    }

    @Override
    public String toString() {
        return "{" +
                "source:'" + source + '\'' +
                ", logback:'" + logback + '\'' +
                ", arg:'" + arg + '\'' +
                ", sourceType:" + sourceType +
                ", verbose:" + verbose +
                ", verboseLevel:" + verboseLevel +
                ", test:" + test +
                ", help:" + help +
                ", version:" + version +
                ", library:" + library +
                ", libraryMode:" + libraryMode +
                ", saveDefaultValue:" + saveDefaultValue +
                '}';
    }

}
