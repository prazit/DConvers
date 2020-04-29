package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataString;
import com.clevel.dconvers.ngin.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class ARGValue extends DynamicValue {

    private DataString dataString;

    public ARGValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(dconvers, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        Converter converter = dconvers.currentConverter;

        int argIndex;
        try {
            argIndex = Integer.parseInt(sourceColumnArg) - 1;
        } catch (Exception ex) {
            log.warn("{}, invalid argument index, first argument is returned", sourceColumnName);
            argIndex = 0;
        }

        String[] args = dconvers.args;
        if (argIndex < 0) {
            log.warn("{}, invalid argument index({}), argument index is start at 1", sourceColumnName, argIndex + 1);
            argIndex = 0;
        } else if (argIndex > args.length) {
            log.warn("{}, invalid argument index({}), last argument index is {}", sourceColumnName, argIndex + 1, args.length);
            argIndex = args.length - 1;
        }

        if (argIndex >= 0) {
            dataString = (DataString) dconvers.createDataColumn(sourceColumnName, Types.VARCHAR, args[argIndex]);
        } else {
            dataString = null;
        }

    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        return dataString;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ARGValue.class);
    }
}
