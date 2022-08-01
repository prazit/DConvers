package com.clevel.dconvers.calc;

import com.clevel.dconvers.DConvers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class CalcFactory {

    private static HashMap<CalcTypes, Calc> calcMap = new HashMap<>();

    /**
     * @param dconvers @NotNull
     * @param calcType @NotNull
     */
    public static Calc getCalc(DConvers dconvers, CalcTypes calcType) {
        Calc calc = calcMap.get(calcType);
        if (calc == null) {

            try {
                Class calcClass = calcType.getCalcClass();
                Constructor constructor = calcClass.getDeclaredConstructor(DConvers.class, String.class);
                constructor.setAccessible(true);
                calc = (Calc) constructor.newInstance(dconvers, calcType.name());
            } catch (InstantiationException e) {
                dconvers.error("The calc({}) cannot be instantiated, {}", calcType.getName(), e.getMessage());
            } catch (IllegalAccessException e) {
                dconvers.error("Create calc({}) is failed, {}", calcType.getName(), e.getMessage());
            } catch (InvocationTargetException e) {
                dconvers.error("InvocationTargetException has occurred when create calc({}), {}", calcType.getName(), e.getMessage());
            } catch (NoSuchMethodException e) {
                dconvers.error("No such method/constructor for calc({}), {}", calcType.getName(), e.getMessage());
            } catch (Exception e) {
                dconvers.error("Unexpected exception, {}", e);
            }

        }

        return calc;
    }

}
