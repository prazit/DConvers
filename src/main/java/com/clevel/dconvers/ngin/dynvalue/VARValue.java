package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VARValue extends DynamicValue {

    private DataColumn dataColumn;

    public VARValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        SystemVariable systemVariable = SystemVariable.parse(sourceColumnArg);
        if (systemVariable == null) {
            valid = false;
            error("Invalid name({}) for system variable of target column({})", sourceColumnName, name);
            dataColumn = null;
            return;
        }

        dataColumn = application.systemVariableMap.get(systemVariable);
        if (dataColumn == null) {
            valid = false;
            error("Invalid systemVariable({}) for target column({})", systemVariable, name);
            dataColumn = null;
        }
    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        if (!isValid()) {
            return null;
        }

        return dataColumn.clone(targetColumnIndex, name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(VARValue.class);
    }
}
