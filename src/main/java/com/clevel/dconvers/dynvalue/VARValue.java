package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
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
            error("Invalid name({}) for system variable, required by target({}.{})", sourceColumnName, targetName, name);
            dataColumn = null;
            return;
        }

        dataColumn = application.systemVariableMap.get(systemVariable);
        if (dataColumn == null) {
            valid = false;
            error("Invalid systemVariable({}), required by target({}.{})", systemVariable, targetName, name);
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
