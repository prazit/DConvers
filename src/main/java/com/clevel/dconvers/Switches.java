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
    private boolean verbose;
    private Level verboseLevel;
    private boolean test;
    private boolean help;
    private boolean version;

    public Switches(Application application) {
        super(application, "switches");
        loadLogger();

        options = new Options();
        registerSwitchesByOptions();

        valid = loadSwitches();
        if (valid) {
            valid = validate();
        }

        log.trace("Switches is created.");
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
            cmd = parser.parse(options, application.args);
        } catch (ParseException e) {
            error("parse switches failed: " + e.getMessage());
            return false;
        }

        String level;
        verbose = cmd.hasOption(Option.VERBOSE.getShortOpt());
        if (verbose) {
            level = cmd.getOptionValue(Option.LEVEL.getShortOpt());
            if (level == null) {
                level = Defaults.VERBOSE_LOG_LEVEL.getStringValue();
            }
        } else {
            level = Defaults.NORMAL_LOG_LEVEL.getStringValue();
        }

        try {
            verboseLevel = Level.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException ex) {
            error("parse verbose level failed: " + ex.getMessage());
            return false;
        }

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
        loggerList.forEach(tmpLogger -> tmpLogger.setLevel(verboseLevel));
        log.trace("Switches.loadSwitches.");
        log.debug("verbose level is {}", verboseLevel.toString());

        source = cmd.getOptionValue(Option.SOURCE.getShortOpt());
        if (source != null && source.lastIndexOf(".") < 0) {
            source = source + Defaults.CONFIG_FILE_EXT.getStringValue();
        }

        test = cmd.hasOption(Option.TEST.getShortOpt());
        help = cmd.hasOption(Option.HELP.getShortOpt());
        version = cmd.hasOption(Option.VERSION.getShortOpt());
        arg = cmd.getOptionValue(Option.ARG.getShortOpt());

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Switches.validateSwitches.");

        if (source == null && !help && !version) {
            error("invalid specified source-file");
            return false;
        }

        return true;
    }

    public String getSource() {
        return source;
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

    public String getArg() {
        return arg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("logback", logback)
                .append("source", source)
                .append("verbose", verbose)
                .append("verboseLevel", verboseLevel)
                .append("help", help)
                .append("valid", valid)
                .toString();
    }
}
