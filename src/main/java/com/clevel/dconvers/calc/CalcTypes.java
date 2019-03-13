package com.clevel.dconvers.calc;

import org.slf4j.LoggerFactory;

public enum CalcTypes {

    COMPILE(CompileCalc.class),
    FORMAT(FormatCalc.class),
    GET(GetCalc.class),
    NAME(NameCalc.class),
    ROWCOUNT(RowCountCalc.class),
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
            name = name.toUpperCase();
            calcType = CalcTypes.valueOf(name);
        } catch (IllegalArgumentException ex) {
            calcType = null;
            LoggerFactory.getLogger(CalcTypes.class).error("CalcTypes.parse(name:{}) is failed! {}", name, ex.getMessage());
        }

        return calcType;
    }
}