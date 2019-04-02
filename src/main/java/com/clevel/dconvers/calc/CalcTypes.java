package com.clevel.dconvers.calc;

import org.slf4j.LoggerFactory;

import java.util.HashMap;

public enum CalcTypes {

    COMPILE(CompileCalc.class),
    FORMAT(FormatCalc.class),
    GET(GetCalc.class),
    NAME(NameCalc.class),
    ROWCOUNT(RowCountCalc.class),
    SUM(SumCalc.class),
    PLUGINS(null);

    private Class calcClass;

    CalcTypes(Class calcClass) {
        this.calcClass = calcClass;
        this.pluginName = null;
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
            calcType = parsePlugins(name);
            if (calcType == null) {
                LoggerFactory.getLogger(CalcTypes.class).error("CalcTypes.parse(name:{}) is failed! {}", name, ex.getMessage());
            }
        }

        return calcType;
    }

    public String getName() {
        if (pluginName == null) {
            return this.name();
        }
        return this.pluginName;
    }

    /* for plugins */

    private String pluginName;
    private static HashMap<String, Class> plugins = new HashMap<>();

    public void forPlugins(String name, Class pluginsClass) {
        this.calcClass = pluginsClass;
        this.pluginName = name;
    }


    private static CalcTypes parsePlugins(String name) {
        name = name.toUpperCase();
        Class pluginsClass = plugins.get(name);
        if (pluginsClass == null) {
            return null;
        }

        CalcTypes calcTypes = CalcTypes.PLUGINS;
        calcTypes.forPlugins(name, pluginsClass);
        return calcTypes;
    }

    public static void addPlugins(String calculatorName, String calculatorClassName) throws ClassNotFoundException {
        Class calcClass;
        calcClass = Class.forName(calculatorClassName);
        plugins.put(calculatorName.toUpperCase(), calcClass);
    }

    @Override
    public String toString() {
        return getName();
    }
}
