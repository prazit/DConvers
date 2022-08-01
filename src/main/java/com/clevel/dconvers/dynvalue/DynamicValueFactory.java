package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DynamicValueFactory {

    /**
     * @param dynamicValueType @NotNull
     * @param dconvers @NotNull
     */
    public static DynamicValue getDynamicValue(DynamicValueType dynamicValueType, DConvers dconvers, String targetName, String targetColumnName, int targetColumnIndex) {
        DynamicValue dynamicValue = null;

        try {
            Class dynamicValueClass = dynamicValueType.getDynamicValueClass();
            Constructor constructor = dynamicValueClass.getDeclaredConstructor(DConvers.class, String.class, String.class, Integer.class);
            constructor.setAccessible(true);
            dynamicValue = (DynamicValue) constructor.newInstance(dconvers, targetName, targetColumnName, targetColumnIndex);
        } catch (InstantiationException e) {
            dconvers.error("The dynamicValue({}) cannot be instantiated, {}", dynamicValueType.name(), e.getMessage());
        } catch (IllegalAccessException e) {
            dconvers.error("Create dynamicValue({}) is failed, {}", dynamicValueType.name(), e.getMessage());
        } catch (InvocationTargetException e) {
            dconvers.error("InvocationTargetException has occurred when create dynamicValue({}), {}", dynamicValueType.name(), e.getMessage());
        } catch (NoSuchMethodException e) {
            dconvers.error("No such method/constructor for dynamicValue({}), {}", dynamicValueType.name(), e.getMessage());
        } catch (Exception e) {
            dconvers.error("Unexpected exception, {}", e);
        }

        return dynamicValue;
    }


}
