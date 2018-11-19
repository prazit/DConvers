package com.clevel.dconvers.ngin.calc;

public enum CalcTypes {

    ROWCOUNT(RowCountCalc.class),
    //VALUE(ValueCalc.class),
    SUM(SumCalc.class);

    private Class calcClass;

    CalcTypes(Class calcClass) {
        this.calcClass = calcClass;
    }

    public Class getCalcClass() {
        return calcClass;
    }

}
