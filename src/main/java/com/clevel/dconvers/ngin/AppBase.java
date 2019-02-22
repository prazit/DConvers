package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import org.slf4j.Logger;

/**
 * Base class for all in this application framework.
 */
public abstract class AppBase extends ValidatorBase {

    protected Application application;
    protected Logger log;

    protected String name;

    public AppBase(Application application, String name) {
        this.application = application;
        this.log = loadLogger();
        this.name = name;
        valid = false;
    }

    public AppBase(String name) {
        this.name = name;
        valid = false;
    }

    public String getName() {
        return name;
    }

    //-- Need Implementation

    /**
     * Load your logger for this properties.
     *
     * @return Logger
     */
    protected abstract Logger loadLogger();

    private void afterError() {
        application.currentState.setValue(application.errorCode);
    }

    private void afterWarn() {
        application.currentState.setValue(application.warningCode);
    }

    private String currentConverterName() {
        if (application.currentConverter == null) {
            return " (no-current-converter)";
        }
        return " (current-converter:" + application.currentConverter.getName() + ")";
    }

    public void error(String format, Object arg1, Object arg2) {
        log.error(format + currentConverterName(), arg1, arg2);
        afterError();
    }

    public void error(String format, Object... arguments) {
        log.error(format + currentConverterName(), arguments);
        afterError();
    }

    public void error(String format, Object arg) {
        log.error(format + currentConverterName(), arg);
        afterError();
    }

    public void error(String msg, Throwable t) {
        log.error(msg + currentConverterName(), t);
        afterError();
    }

    public void error(String msg) {
        log.error(msg + currentConverterName());
        afterError();
    }

}
