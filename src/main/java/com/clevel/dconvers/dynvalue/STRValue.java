package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class STRValue extends DynamicValue {

    private DataString dataString;

    public STRValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
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

        dataString = (DataString) dconvers.createDataColumn(1, name, sourceColumnType.getDataType(), sourceColumnArg);
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
