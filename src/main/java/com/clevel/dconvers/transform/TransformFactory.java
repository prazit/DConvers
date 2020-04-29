package com.clevel.dconvers.transform;

import com.clevel.dconvers.DConvers;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class TransformFactory {

    private static HashMap<TransformTypes, Transform> transformMap = new HashMap<>();

    public static Transform getTransform(@NotNull DConvers dconvers, @NotNull TransformTypes transformType) {
        Transform transform = transformMap.get(transformType);
        if (transform == null) {

            try {
                Class transformClass = transformType.getTransformClass();
                Constructor constructor = transformClass.getDeclaredConstructor(DConvers.class, String.class);
                constructor.setAccessible(true);
                transform = (Transform) constructor.newInstance(dconvers, transformType.name());
            } catch (InstantiationException e) {
                dconvers.error("The transform({}) cannot be instantiated, {}", transformType.name(), e.getMessage());
            } catch (IllegalAccessException e) {
                dconvers.error("Create transform({}) is failed, {}", transformType.name(), e.getMessage());
            } catch (InvocationTargetException e) {
                dconvers.error("InvocationTargetException has occurred when create transform({}), {}", transformType.name(), e.getMessage());
            } catch (NoSuchMethodException e) {
                dconvers.error("No such method/constructor for transform({}), {}", transformType.name(), e.getMessage());
            } catch (Exception e) {
                dconvers.error("Unexpected exception, {}", e);
            }

        }

        return transform;
    }

}
