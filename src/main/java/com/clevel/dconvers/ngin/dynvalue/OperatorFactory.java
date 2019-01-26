package com.clevel.dconvers.ngin.dynvalue;

import com.clevel.dconvers.Application;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class OperatorFactory {

    private static HashMap<OperatorType, Operator> operatorMap = new HashMap<>();

    public static Operator getOperator(@NotNull Application application, @NotNull OperatorType operatorType) {
        Operator operator = operatorMap.get(operatorType);
        if (operator == null) {

            try {
                Class outputClass = operatorType.getOperatorClass();
                Constructor constructor = outputClass.getDeclaredConstructor(Application.class, String.class);
                constructor.setAccessible(true);
                operator = (Operator) constructor.newInstance(application, operatorType.name());
            } catch (InstantiationException e) {
                application.error("The operator({}) cannot be instantiated, {}", operatorType.name(), e.getMessage());
            } catch (IllegalAccessException e) {
                application.error("Create operator({}) is failed, {}", operatorType.name(), e.getMessage());
            } catch (InvocationTargetException e) {
                application.error("InvocationTargetException has occurred when create operator({}), {}", operatorType.name(), e.getMessage());
            } catch (NoSuchMethodException e) {
                application.error("No such method/constructor for operator({}), {}", operatorType.name(), e.getMessage());
            } catch (Exception e) {
                application.error("Unexpected exception, {}", e);
            }

        }

        return operator;
    }

}
