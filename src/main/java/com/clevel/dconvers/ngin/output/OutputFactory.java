package com.clevel.dconvers.ngin.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Output;
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class OutputFactory {

    private static Map<OutputTypes, Output> outputMap = new HashMap<>();

    public static Output getOutput(@NotNull Application application, @NotNull OutputTypes outputType) {
        Logger log = LoggerFactory.getLogger(OutputFactory.class);

        Output output = outputMap.get(outputType);
        if (output == null) {

            try {
                Class outputClass = outputType.getOutputClass();
                Constructor constructor = outputClass.getDeclaredConstructor(Application.class, String.class);
                constructor.setAccessible(true);
                output = (Output) constructor.newInstance(application, outputType.name());
            } catch (InstantiationException e) {
                log.error("The output({}) cannot be instantiated, {}", outputType.name(), e.getMessage());
            } catch (IllegalAccessException e) {
                log.error("Create output({}) is failed, {}", outputType.name(), e.getMessage());
            } catch (InvocationTargetException e) {
                log.error("InvocationTargetException has occurred when create output({}), {}", outputType.name(), e.getMessage());
            } catch (NoSuchMethodException e) {
                log.error("No such method/constructor for output({}), {}", outputType.name(), e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected exception, {}", e);
            }

        }

        return output;
    }

}
