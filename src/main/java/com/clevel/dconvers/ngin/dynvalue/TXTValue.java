package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class TXTValue extends DynamicValue {

    private DataString dataString;

    public TXTValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        Converter converter = application.currentConverter;

        String value = converter.valueFromFile(sourceColumnName);
        if (value == null) {
            dataString = null;
        } else {
            dataString = (DataString) application.createDataColumn(DynamicValueType.TXT.name(), Types.VARCHAR, value);
        }

    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        return dataString;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(TXTValue.class);
    }
}
