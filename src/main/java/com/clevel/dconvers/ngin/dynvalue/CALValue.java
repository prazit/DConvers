package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.calc.Calc;
import com.clevel.dconvers.ngin.calc.CalcFactory;
import com.clevel.dconvers.ngin.calc.CalcTypes;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CALValue extends DynamicValue{

    private Calc calculator;
    private String calcName;
    private String arguments;

    public CALValue(Application application, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(application, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        String[] values = sourceColumnArg.split("[()]");
        calcName = values[0];
        arguments = values[1];

        CalcTypes calcType = CalcTypes.parse(calcName);
        if (calcType == null) {
            valid = false;
            error("Invalid Calculator({}) for target column({})", sourceColumnArg, name);
            calculator = null;
            return;
        }

        calculator = CalcFactory.getCalc(application, calcType);
        if (calculator == null) {
            valid = false;
            calculator = null;
            return;
        }
        calculator.setArguments(arguments);
    }

    @Override
    public DataColumn getValue(DataRow sourceRow) {
        if (!isValid()) {
            return null;
        }

        if (calculator == null) {
            return null;
        }

        DataColumn targetColumn = calculator.calc();
        if (targetColumn == null) {
            return null;
        }

        targetColumn.setName(name);
        return targetColumn;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(CALValue.class);
    }
}
