package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class STRValue extends DynamicValue {

    private DataString dataString;

    public STRValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
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

        dataString = (DataString) application.createDataColumn(name, sourceColumnType.getDataType(), sourceColumnArg);
        if (dataString == null) {
            error("Invalid constant({}) for columnType({}) that required by target({}.{})", sourceColumnArg, sourceColumnType.name(), targetName, name);
            dataString = null;
            return;
        }

        if (dataString.getType() == Types.VARCHAR) {
            String value = dataString.getValue();
            value = converter.compileDynamicValues(value);
            dataString.setValue(value);
        }

    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        return dataString;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(STRValue.class);
    }
}
