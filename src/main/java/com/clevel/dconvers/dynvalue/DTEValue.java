package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataDate;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DTEValue extends DynamicValue {

    private DataDate dataDate;

    public DTEValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(dconvers, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        Converter converter = dconvers.currentConverter;

        if (sourceColumnArg.compareToIgnoreCase("NULL") == 0) {
            sourceColumnArg = null;
        } else if (sourceColumnArg.contains("$[")) {
            sourceColumnArg = converter.compileDynamicValues(sourceColumnArg);
        }

        dataDate = (DataDate) dconvers.createDataColumn(name, sourceColumnType.getDataType(), sourceColumnArg);
        if (dataDate == null) {
            error("Invalid constant({}) for columnType({}) that required by target({}.{})", sourceColumnArg, sourceColumnType.name(), targetName, name);
            dataDate = null;
            return;
        }

    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        return dataDate;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DTEValue.class);
    }
}
