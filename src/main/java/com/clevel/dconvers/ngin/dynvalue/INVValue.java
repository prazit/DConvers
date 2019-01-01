package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INVValue extends DynamicValue {

    public INVValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        valid = false;
        error("Invalid source-column({}) for target-column({})", sourceColumnName, name);
    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        return null;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(INVValue.class);
    }
}
