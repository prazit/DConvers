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

}
