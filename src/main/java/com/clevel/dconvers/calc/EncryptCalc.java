package com.clevel.dconvers.calc;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.ngin.Crypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptCalc extends GetCalc {

    public EncryptCalc(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    protected boolean prepareValue() {
        boolean success = super.prepareValue();
        if (!success) {
            return false;
        }

        value.setValue(Crypto.encrypt(value.getValue()));
        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(EncryptCalc.class);
    }
}
