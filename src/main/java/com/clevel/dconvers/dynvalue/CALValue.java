package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.calc.Calc;
import com.clevel.dconvers.calc.CalcFactory;
import com.clevel.dconvers.calc.CalcTypes;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CALValue extends DynamicValue {

    private Calc calculator;
    private String calcName;
    private String arguments;

    public CALValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(dconvers, targetName, targetColumnName, targetColumnIndex);
    }

    @Override
    public void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg) {
        String[] values = sourceColumnArg.split("[()]");
        calcName = values[0];
        arguments = values[1];

        CalcTypes calcType = CalcTypes.parse(calcName);
        if (calcType == null) {
            valid = false;
            error("Invalid Calculator({}) that required by target({}.{})", sourceColumnArg, targetName, name);
            calculator = null;
            return;
        }

        calculator = CalcFactory.getCalc(dconvers, calcType);
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
