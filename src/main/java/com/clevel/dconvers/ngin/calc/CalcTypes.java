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

    public static CalcTypes parse(String name) {
        CalcTypes calcType;

        try {
            calcType = CalcTypes.valueOf(name);
        } catch (IllegalArgumentException ex) {
            calcType = null;
        }

        return calcType;
    }
}
