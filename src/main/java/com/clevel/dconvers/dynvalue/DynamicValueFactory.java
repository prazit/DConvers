package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.Application;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DynamicValueFactory {


    public static DynamicValue getDynamicValue(@NotNull DynamicValueType dynamicValueType, @NotNull Application application, String targetName, String targetColumnName, int targetColumnIndex) {
        DynamicValue dynamicValue = null;

        try {
            Class dynamicValueClass = dynamicValueType.getDynamicValueClass();
            Constructor constructor = dynamicValueClass.getDeclaredConstructor(Application.class, String.class, String.class, Integer.class);
            constructor.setAccessible(true);
            dynamicValue = (DynamicValue) constructor.newInstance(application, targetName, targetColumnName, targetColumnIndex);
        } catch (InstantiationException e) {
            application.error("The dynamicValue({}) cannot be instantiated, {}", dynamicValueType.name(), e.getMessage());
        } catch (IllegalAccessException e) {
            application.error("Create dynamicValue({}) is failed, {}", dynamicValueType.name(), e.getMessage());
        } catch (InvocationTargetException e) {
            application.error("InvocationTargetException has occurred when create dynamicValue({}), {}", dynamicValueType.name(), e.getMessage());
        } catch (NoSuchMethodException e) {
            application.error("No such method/constructor for dynamicValue({}), {}", dynamicValueType.name(), e.getMessage());
        } catch (Exception e) {
            application.error("Unexpected exception, {}", e);
        }

        return dynamicValue;
    }


}
