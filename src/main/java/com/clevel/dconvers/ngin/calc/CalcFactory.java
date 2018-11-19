package com.clevel.dconvers.ngin.calc;

import com.clevel.dconvers.Application;
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CalcFactory {

    private static Map<CalcTypes, Calc> calcMap = new HashMap<>();

    public static Calc getCalc(@NotNull Application application, @NotNull CalcTypes calcType) {
        Logger log = LoggerFactory.getLogger(CalcFactory.class);

        Calc calc = calcMap.get(calcType);
        if (calc == null) {

            try{
                Class calcClass = calcType.getCalcClass();
                Constructor constructor = calcClass.getDeclaredConstructor(Application.class, String.class);
                constructor.setAccessible(true);
                calc = (Calc) constructor.newInstance(application,calcType.name());
            } catch (InstantiationException e) {
                log.error("The calc({}) cannot be instantiated, {}", calcType.name(), e.getMessage());
            } catch (IllegalAccessException e) {
                log.error("Create calc({}) is failed, {}", calcType.name(), e.getMessage());
            } catch (InvocationTargetException e) {
                log.error("InvocationTargetException has occurred when create calc({}), {}", calcType.name(), e.getMessage());
            } catch (NoSuchMethodException e) {
                log.error("No such method/constructor for calc({}), {}", calcType.name(), e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected exception, {}", e);
            }

        }

        return calc;
    }

}
