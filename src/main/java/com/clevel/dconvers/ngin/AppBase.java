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
        application.currentState.setValue((long) application.dataConversionConfigFile.getErrorCode());
    }

    private void afterWarn() {
        application.currentState.setValue((long) application.dataConversionConfigFile.getWarningCode());
    }

    public void error(String format, Object arg1, Object arg2) {
        error(format, arg1, arg2);
        afterError();
    }

    public void error(String format, Object... arguments) {
        error(format, arguments);
        afterError();
    }

    public void error(String format, Object arg) {
        error(format, arg);
        afterError();
    }

    public void error(String msg, Throwable t) {
        error(msg, t);
        afterError();
    }

    public void error(String msg) {
        error(msg);
        afterError();
    }

}
