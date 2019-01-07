package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class TransformFactory {

    private static HashMap<TransformTypes, Transform> transformMap = new HashMap<>();

    public static Transform getTransform(@NotNull Application application, @NotNull TransformTypes transformType) {
        Transform transform = transformMap.get(transformType);
        if (transform == null) {

            try {
                Class transformClass = transformType.getTransformClass();
                Constructor constructor = transformClass.getDeclaredConstructor(Application.class, String.class);
                constructor.setAccessible(true);
                transform = (Transform) constructor.newInstance(application, transformType.name());
            } catch (InstantiationException e) {
                application.error("The transform({}) cannot be instantiated, {}", transformType.name(), e.getMessage());
            } catch (IllegalAccessException e) {
                application.error("Create transform({}) is failed, {}", transformType.name(), e.getMessage());
            } catch (InvocationTargetException e) {
                application.error("InvocationTargetException has occurred when create transform({}), {}", transformType.name(), e.getMessage());
            } catch (NoSuchMethodException e) {
                application.error("No such method/constructor for transform({}), {}", transformType.name(), e.getMessage());
            } catch (Exception e) {
                application.error("Unexpected exception, {}", e);
            }

        }

        return transform;
    }

}
