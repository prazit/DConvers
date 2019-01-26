package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.AppBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Operator extends AppBase {

    public Operator(Application application, String name) {
        super(application, name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Operator.class);
    }

    public abstract String compute(String leftOperand, String rightOperand);
}
