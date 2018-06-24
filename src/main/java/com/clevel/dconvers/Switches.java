package com.clevel.dconvers;

import ch.qos.logback.classic.LoggerContext;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.ngin.ValidatorBase;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

import java.util.List;

public class Switches extends ValidatorBase {

    private Application application;
    private Logger log;
    private CommandLine cmd;

    private Options options;

    private String source;
    private boolean verbose;
    private Level verboseLevel;
    private boolean help;

    public Switches(Application application) {
        this.application = application;
        log = LoggerFactory.getLogger(Switches.class);
        options = new Options();

        registerSwitches();
        loadSwitches();

        valid = validate();
        log.trace("Switches is created.");
    }

    private void registerSwitches() {
        options.addRequiredOption("s", "source", true, "source file is required for data conversion, please see 'sample-source.conf' for detailed");
        options.addOption("t", "verbose", false, "run in verbose mode will show TRACE level of messages");
        options.addOption("v", "verbose-level", true, "specified level will use as level of messages\npossible values are TRACE, DEBUG, ERROR, WARNING, INFO");
        options.addOption("h", "help", false, "print help message to console");
    }

    private void loadSwitches() {
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, application.args);
        } catch (ParseException e) {
            log.error("parse switches failed: " + e.getMessage());
            application.stopWithError();
        }

        verbose = cmd.hasOption("v");
        String level = cmd.getOptionValue("v");
        if (level == null) {
            level = Defaults.VERBOSE_LEVEL.getDefaultValue();
        }
        try {
            verboseLevel = Level.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException ex) {
            verboseLevel = Level.TRACE;
        }
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
        loggerList.forEach(tmpLogger -> tmpLogger.setLevel(verboseLevel));
        log.trace("Switches.loadSwitches.");
        log.debug("verbose level is {}", verboseLevel.toString());

        source = cmd.getOptionValue("s");
        if (source != null && source.lastIndexOf(".") < 0) {
            source = source + Defaults.CONFIG_FILE_EXT.getDefaultValue();
        }

        help = cmd.hasOption("h");
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(super.toString())
                .append("source", source)
                .append("verbose", verbose)
                .append("verboseLevel", verboseLevel)
                .append("help", help)
                .append("valid", valid)
                .toString();
    }
}
