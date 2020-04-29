package com.clevel.dconvers.input;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class InputFactory {

    private static HashMap<String, DataSource> inputMap = new HashMap<>();
    private static HashMap<String, DataSourceConfig> dataSourceConfigMap = new HashMap<>();

    public static DataSource getDataSource(@NotNull DConvers dconvers, @NotNull String name, @NotNull String className) {
        DataSource dataSource = inputMap.get(name);
        if (dataSource == null) {
            DataSourceConfig dataSourceConfig = new DataSourceConfig(dconvers, name);

            try {
                Class inputClass = Class.forName(className);
                Constructor constructor = inputClass.getDeclaredConstructor(DConvers.class, String.class, DataSourceConfig.class);
                constructor.setAccessible(true);
                dataSource = (DataSource) constructor.newInstance(dconvers, name, dataSourceConfig);
            } catch (InstantiationException e) {
                dconvers.error("The dataSource({}) cannot be instantiated, {}", name, e.getMessage());
            } catch (IllegalAccessException e) {
                dconvers.error("Create dataSource({}) is failed, {}", name, e.getMessage());
            } catch (InvocationTargetException e) {
                dconvers.error("InvocationTargetException has occurred when create dataSource({}), {}", name, e.getMessage());
            } catch (NoSuchMethodException e) {
                dconvers.error("No such method/constructor for dataSource({}), {}", name, e.getMessage());
            } catch (Exception e) {
                dconvers.error("Unexpected exception, {}", e);
            }

        }

        return dataSource;
    }

}
