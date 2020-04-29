package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataBigDecimal;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DECValue extends DynamicValue {

    private DataBigDecimal dataBigDecimal;

    public DECValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
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

        dataBigDecimal = (DataBigDecimal) dconvers.createDataColumn(name, sourceColumnType.getDataType(), sourceColumnArg);

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
