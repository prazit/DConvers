package com.clevel.dconvers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.ngin.ValidatorBase;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;

public class Switches extends ValidatorBase {

    private Application application;
    private Logger log;
    private CommandLine cmd;

    private Options options;

    private String source;
    private String logback;
    private boolean verbose;
    private Level verboseLevel;
    private boolean help;

    public Switches(Application application) {
        this.application = application;
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

    private void loadLogger() {
        log = LoggerFactory.getLogger(Switches.class);
    }

    private boolean loadSwitches() {
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, application.args);
        } catch (ParseException e) {
            log.error("parse switches failed: " + e.getMessage());
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
            log.error("parse verbose level failed: " + ex.getMessage());
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

        help = cmd.hasOption(Option.HELP.getShortOpt());

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Switches.validateSwitches.");

        if (source == null && !help) {
            log.error("invalid specified source-file");
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

    public boolean isHelp() {
        return help;
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
