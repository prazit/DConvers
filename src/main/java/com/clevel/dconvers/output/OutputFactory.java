package com.clevel.dconvers.output;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.OutputPluginConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class OutputFactory {

    private static HashMap<OutputTypes, Output> outputMap = new HashMap<>();
    private static HashMap<OutputTypes, OutputPluginConfig> outputConfigMap = new HashMap<>();

    /**
     * @param dconvers @NotNull
     * @param outputType @NotNull
     */
    public static Output getOutput(DConvers dconvers, OutputTypes outputType) {
        Output output = outputMap.get(outputType);
        if (output == null) {

            try {
                Class outputClass = outputType.getOutputClass();
                Constructor constructor = outputClass.getDeclaredConstructor(DConvers.class, String.class);
                constructor.setAccessible(true);
                output = (Output) constructor.newInstance(dconvers, outputType.getName());
            } catch (InstantiationException e) {
                dconvers.error("The output({}) cannot be instantiated, {}", outputType.getName(), e.getMessage());
            } catch (IllegalAccessException e) {
                dconvers.error("Create output({}) is failed, {}", outputType.getName(), e.getMessage());
            } catch (InvocationTargetException e) {
                dconvers.error("InvocationTargetException has occurred when create output({}), {}", outputType.getName(), e.getMessage());
            } catch (NoSuchMethodException e) {
                dconvers.error("No such method/constructor for output({}), {}", outputType.getName(), e.getMessage());
            } catch (Exception e) {
                dconvers.error("Unexpected exception, {}", e);
            }

        }

        return output;
    }

    /**
     * @param dconvers @NotNull
     * @param outputType @NotNull
     */
    public static OutputPluginConfig getPluginConfig(DConvers dconvers, OutputTypes outputType) {
        OutputPluginConfig config = outputConfigMap.get(outputType);
        if (config == null) {

            try {
                Class outputClass = outputType.getOutputConfigClass();
                Constructor constructor = outputClass.getDeclaredConstructor(DConvers.class, String.class);
                constructor.setAccessible(true);
                config = (OutputPluginConfig) constructor.newInstance(dconvers, outputType.getName());
            } catch (InstantiationException e) {
                dconvers.error("The pluginConfig({}) cannot be instantiated, {}", outputType.getName(), e.getMessage());
            } catch (IllegalAccessException e) {
                dconvers.error("Create pluginConfig({}) is failed, {}", outputType.getName(), e.getMessage());
            } catch (InvocationTargetException e) {
                dconvers.error("InvocationTargetException has occurred when create pluginConfig({}), {}", outputType.getName(), e.getMessage());
            } catch (NoSuchMethodException e) {
                dconvers.error("No such method/constructor for pluginConfig({}), {}", outputType.getName(), e.getMessage());
            } catch (Exception e) {
                dconvers.error("Unexpected exception, {}", e);
            }

        }

        return config;
    }

}
