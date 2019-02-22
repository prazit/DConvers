package com.clevel.dconvers.calc;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.ngin.UtilBase;

import java.sql.Types;

public abstract class Calc extends UtilBase {

    private String arguments;
    private boolean prepared;

    public void setArguments(String arguments) {
        this.arguments = arguments;
        prepared = false;
    }

    protected String getArguments() {
        return arguments;
    }

    public Calc(Application application, String name) {
        super(application, name);

        prepared = false;
    }

    protected abstract boolean prepare();

    protected abstract DataColumn calculate();

    public DataColumn calc() {
        if (!prepared) {
            if (!prepare()) {
                return new DataString(application, 0, Types.VARCHAR, "prepare_is_failed", "");
            }
            prepared = true;
        }

        return calculate();
    }

}
