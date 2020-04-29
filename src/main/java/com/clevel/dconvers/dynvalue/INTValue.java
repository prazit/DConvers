package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataLong;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INTValue extends DynamicValue {

    private DataLong dataLong;

    public INTValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
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

        dataLong = (DataLong) dconvers.createDataColumn(name, sourceColumnType.getDataType(), sourceColumnArg);
        if (dataLong == null) {
            error("Invalid constant({}) for columnType({}) that required by target({}.{})", sourceColumnArg, sourceColumnType.name(), targetName, name);
            dataLong = null;
            return;
        }

    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        return dataLong;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(INTValue.class);
    }
}
