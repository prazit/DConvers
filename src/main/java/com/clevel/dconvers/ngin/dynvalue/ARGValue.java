package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class ARGValue extends DynamicValue {

    private DataString dataString;

    public ARGValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        Converter converter = application.currentConverter;

        int argIndex;
        try {
            argIndex = Integer.parseInt(sourceColumnArg) - 1;
        } catch (Exception ex) {
            log.warn("{}, invalid argument index, first argument is returned", sourceColumnName);
            argIndex = 0;
        }

        String[] args = application.args;
        if (argIndex < 0) {
            log.warn("{}, invalid argument index({}), argument index is start at 1", sourceColumnName, argIndex + 1);
            argIndex = 0;
        } else if (argIndex > args.length) {
            log.warn("{}, invalid argument index({}), last argument index is {}", sourceColumnName, args.length);
            argIndex = args.length - 1;
        }

        if (argIndex >= 0) {
            dataString = (DataString) application.createDataColumn(sourceColumnName, Types.VARCHAR, args[argIndex]);
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
