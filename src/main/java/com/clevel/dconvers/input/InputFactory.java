package com.clevel.dconvers.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class InputFactory {

    private static HashMap<String, DataSource> inputMap = new HashMap<>();
    private static HashMap<String, DataSourceConfig> dataSourceConfigMap = new HashMap<>();

    public static DataSource getDataSource(@NotNull Application application, @NotNull String name, @NotNull String className) {
        DataSource dataSource = inputMap.get(name);
        if (dataSource == null) {
            DataSourceConfig dataSourceConfig = new DataSourceConfig(application, name);

            try {
                Class inputClass = Class.forName(className);
                Constructor constructor = inputClass.getDeclaredConstructor(Application.class, String.class, DataSourceConfig.class);
                constructor.setAccessible(true);
                dataSource = (DataSource) constructor.newInstance(application, name, dataSourceConfig);
            } catch (InstantiationException e) {
                application.error("The dataSource({}) cannot be instantiated, {}", name, e.getMessage());
            } catch (IllegalAccessException e) {
                application.error("Create dataSource({}) is failed, {}", name, e.getMessage());
            } catch (InvocationTargetException e) {
                application.error("InvocationTargetException has occurred when create dataSource({}), {}", name, e.getMessage());
            } catch (NoSuchMethodException e) {
                application.error("No such method/constructor for dataSource({}), {}", name, e.getMessage());
            } catch (Exception e) {
                application.error("Unexpected exception, {}", e);
            }

        }

        return dataSource;
    }

}
