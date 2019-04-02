package com.clevel.dconvers.calc;

import com.clevel.dconvers.Application;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class CalcFactory {

    private static HashMap<CalcTypes, Calc> calcMap = new HashMap<>();

    public static Calc getCalc(@NotNull Application application, @NotNull CalcTypes calcType) {
        Calc calc = calcMap.get(calcType);
        if (calc == null) {

            try {
                Class calcClass = calcType.getCalcClass();
                Constructor constructor = calcClass.getDeclaredConstructor(Application.class, String.class);
                constructor.setAccessible(true);
                calc = (Calc) constructor.newInstance(application, calcType.name());
            } catch (InstantiationException e) {
                application.error("The calc({}) cannot be instantiated, {}", calcType.getName(), e.getMessage());
            } catch (IllegalAccessException e) {
                application.error("Create calc({}) is failed, {}", calcType.getName(), e.getMessage());
            } catch (InvocationTargetException e) {
                application.error("InvocationTargetException has occurred when create calc({}), {}", calcType.getName(), e.getMessage());
            } catch (NoSuchMethodException e) {
                application.error("No such method/constructor for calc({}), {}", calcType.getName(), e.getMessage());
            } catch (Exception e) {
                application.error("Unexpected exception, {}", e);
            }

        }

        return calc;
    }

}
