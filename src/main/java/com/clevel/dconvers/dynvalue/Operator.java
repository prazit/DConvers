package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.ngin.AppBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Operator extends AppBase {

    public Operator(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Operator.class);
    }

    public abstract String compute(String leftOperand, String rightOperand);
}
