package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NONValue extends DynamicValue {

    private String sourceName;
    private String sourceColumnName;

    public NONValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        this.sourceName = sourceName;
        this.sourceColumnName = sourceColumnName;
    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        DataColumn dataColumn = sourceRow.getColumn(sourceColumnName);
        if (dataColumn == null) {
            error("No column({}) in source({})", sourceColumnName, sourceName);
            return null;
        }

        return dataColumn.clone(targetColumnIndex, name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(NONValue.class);
    }
}