package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class OperatorFactory {

    private static HashMap<OperatorType, Operator> operatorMap = new HashMap<>();

    /**
     * @param dconvers @NotNull
     * @param operatorType @NotNull
     */
    public static Operator getOperator(DConvers dconvers, OperatorType operatorType) {
        Operator operator = operatorMap.get(operatorType);
        if (operator == null) {

            try {
                Class outputClass = operatorType.getOperatorClass();
                Constructor constructor = outputClass.getDeclaredConstructor(DConvers.class, String.class);
                constructor.setAccessible(true);
                operator = (Operator) constructor.newInstance(dconvers, operatorType.name());
            } catch (InstantiationException e) {
                dconvers.error("The operator({}) cannot be instantiated, {}", operatorType.name(), e.getMessage());
            } catch (IllegalAccessException e) {
                dconvers.error("Create operator({}) is failed, {}", operatorType.name(), e.getMessage());
            } catch (InvocationTargetException e) {
                dconvers.error("InvocationTargetException has occurred when create operator({}), {}", operatorType.name(), e.getMessage());
            } catch (NoSuchMethodException e) {
                dconvers.error("No such method/constructor for operator({}), {}", operatorType.name(), e.getMessage());
            } catch (Exception e) {
                dconvers.error("Unexpected exception, {}", e);
            }

        }

        return operator;
    }

}
