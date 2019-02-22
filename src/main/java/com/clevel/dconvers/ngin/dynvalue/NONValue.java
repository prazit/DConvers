package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NONValue extends DynamicValue {

    private Converter converter;
    private String sourceName;
    private String sourceColumnName;

    public NONValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        this.sourceName = sourceName;
        this.sourceColumnName = sourceColumnName;
        converter = application.currentConverter;
    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        DataColumn dataColumn = sourceRow.getColumn(sourceColumnName);
        if (dataColumn == null) {
            error("No column({}) in source({}) in converter({}) that required by target({},{})", sourceColumnName, sourceName, converter.getName(), targetName, name);
            return null;
        }

        return dataColumn.clone(targetColumnIndex, name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(NONValue.class);
    }
}
