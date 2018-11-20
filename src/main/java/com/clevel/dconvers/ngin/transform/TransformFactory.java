package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class TransformFactory {

    private static Map<TransformTypes, Transform> transformMap = new HashMap<>();

    public static Transform getTransform(@NotNull Application application, @NotNull TransformTypes transformType) {
        Logger log = LoggerFactory.getLogger(TransformFactory.class);

        Transform transform = transformMap.get(transformType);
        if (transform == null) {

            try {
                Class transformClass = transformType.getTransformClass();
                Constructor constructor = transformClass.getDeclaredConstructor(Application.class, String.class);
                constructor.setAccessible(true);
                transform = (Transform) constructor.newInstance(application, transformType.name());
            } catch (InstantiationException e) {
                log.error("The transform({}) cannot be instantiated, {}", transformType.name(), e.getMessage());
            } catch (IllegalAccessException e) {
                log.error("Create transform({}) is failed, {}", transformType.name(), e.getMessage());
            } catch (InvocationTargetException e) {
                log.error("InvocationTargetException has occurred when create transform({}), {}", transformType.name(), e.getMessage());
            } catch (NoSuchMethodException e) {
                log.error("No such method/constructor for transform({}), {}", transformType.name(), e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected exception, {}", e);
            }

        }

        return transform;
    }

}