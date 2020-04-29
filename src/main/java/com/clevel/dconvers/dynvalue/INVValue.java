package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INVValue extends DynamicValue {

    public INVValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(dconvers, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        valid = false;
        error("Invalid source-column({}) for target({}.{})", sourceColumnName, targetName, name);
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
