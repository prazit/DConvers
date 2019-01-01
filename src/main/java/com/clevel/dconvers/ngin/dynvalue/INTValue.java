package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataLong;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INTValue extends DynamicValue {

    private DataLong dataLong;

    public INTValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        Converter converter = application.currentConverter;

        if (sourceColumnArg.compareToIgnoreCase("NULL") == 0) {
            sourceColumnArg = null;
        } else if (sourceColumnArg.contains("$[")) {
            sourceColumnArg = converter.compileDynamicValues(sourceColumnArg);
        }

        dataLong = (DataLong) application.createDataColumn(name, sourceColumnType.getDataType(), sourceColumnArg);
        if (dataLong == null) {
            error("Invalid constant({}) for {} that required by target column({})", sourceColumnArg, sourceColumnType.name(), name);
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
