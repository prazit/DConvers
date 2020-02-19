package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class HTTPValue extends DynamicValue {

    private DataString dataString;

    public HTTPValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        Converter converter = application.currentConverter;

        String value = converter.valueFromHttp(sourceColumnName);
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
        return LoggerFactory.getLogger(HTTPValue.class);
    }
}
