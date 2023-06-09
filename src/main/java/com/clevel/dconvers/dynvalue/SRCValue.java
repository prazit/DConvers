package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class SRCValue extends DynamicValue {

    private DataColumn dataColumn;

    public SRCValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(dconvers, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        String[] dataTableParameters = sourceColumnName.split("[.]");
        if (dataTableParameters.length == 1) {
            dataColumn = null;
            return;
        }

        String value = dconvers.currentConverter.valuesFromDataTable(dataTableParameters[0], dataTableParameters[1]);
        if (value == null) {
            dataColumn = null;
        } else {
            dataColumn = dconvers.createDataColumn(1, sourceColumnType.name(), Types.VARCHAR, value);
        }
    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        return dataColumn;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SRCValue.class);
    }
}
