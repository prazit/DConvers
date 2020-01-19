package com.clevel.dconvers.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputPluginConfig;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class OutputFactory {

    private static HashMap<OutputTypes, Output> outputMap = new HashMap<>();
    private static HashMap<OutputTypes, OutputPluginConfig> outputConfigMap = new HashMap<>();

    public static Output getOutput(@NotNull Application application, @NotNull OutputTypes outputType) {
        Output output = outputMap.get(outputType);
        if (output == null) {

            try {
                Class outputClass = outputType.getOutputClass();
                Constructor constructor = outputClass.getDeclaredConstructor(Application.class, String.class);
                constructor.setAccessible(true);
                output = (Output) constructor.newInstance(application, outputType.getName());
            } catch (InstantiationException e) {
                application.error("The output({}) cannot be instantiated, {}", outputType.getName(), e.getMessage());
            } catch (IllegalAccessException e) {
                application.error("Create output({}) is failed, {}", outputType.getName(), e.getMessage());
            } catch (InvocationTargetException e) {
                application.error("InvocationTargetException has occurred when create output({}), {}", outputType.getName(), e.getMessage());
            } catch (NoSuchMethodException e) {
                application.error("No such method/constructor for output({}), {}", outputType.getName(), e.getMessage());
            } catch (Exception e) {
                application.error("Unexpected exception, {}", e);
            }

        }

        return output;
    }

    public static OutputPluginConfig getPluginConfig(@NotNull Application application, @NotNull OutputTypes outputType) {
        OutputPluginConfig config = outputConfigMap.get(outputType);
        if (config == null) {

            try {
                Class outputClass = outputType.getOutputConfigClass();
                Constructor constructor = outputClass.getDeclaredConstructor(Application.class, String.class);
                constructor.setAccessible(true);
                config = (OutputPluginConfig) constructor.newInstance(application, outputType.getName());
            } catch (InstantiationException e) {
                application.error("The pluginConfig({}) cannot be instantiated, {}", outputType.getName(), e.getMessage());
            } catch (IllegalAccessException e) {
                application.error("Create pluginConfig({}) is failed, {}", outputType.getName(), e.getMessage());
            } catch (InvocationTargetException e) {
                application.error("InvocationTargetException has occurred when create pluginConfig({}), {}", outputType.getName(), e.getMessage());
            } catch (NoSuchMethodException e) {
                application.error("No such method/constructor for pluginConfig({}), {}", outputType.getName(), e.getMessage());
            } catch (Exception e) {
                application.error("Unexpected exception, {}", e);
            }

        }

        return config;
    }

}
