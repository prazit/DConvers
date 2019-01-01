package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.data.DataBigDecimal;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DECValue extends DynamicValue {

    private DataBigDecimal dataBigDecimal;

    public DECValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
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

        dataBigDecimal = (DataBigDecimal) application.createDataColumn(name, sourceColumnType.getDataType(), sourceColumnArg);

    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        return dataBigDecimal;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DECValue.class);
    }
}
