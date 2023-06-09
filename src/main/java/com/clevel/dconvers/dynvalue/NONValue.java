package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NONValue extends DynamicValue {

    private Converter converter;
    private String sourceName;
    private String sourceColumnName;

    public NONValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(dconvers, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        this.sourceName = sourceName;
        this.sourceColumnName = sourceColumnName;
        converter = dconvers.currentConverter;
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
