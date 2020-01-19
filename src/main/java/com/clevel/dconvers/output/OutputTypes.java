package com.clevel.dconvers.output;

import org.slf4j.LoggerFactory;

import java.util.HashMap;

public enum OutputTypes {

    // first  used: SCT Data Migration
    SQL_FILE(SQLOutput.class),
    MARKDOWN_FILE(MarkdownOutput.class),

    // first used: TMB ECM ENoti(internal)
    PDF_FILE(PDFOutput.class),

    // first used: LHBank ETL
    TXT_FILE(TXTOutput.class),
    CSV_FILE(CSVOutput.class),
    INSERT_DB(DBInsertOutput.class),
    UPDATE_DB(DBUpdateOutput.class),
    EXECUTE_DB(DBExecuteOutput.class),
    OS_VARIABLE(OSVariableOutput.class),

    CONVERTER_SOURCE_FILE(SRCOutput.class),
    CONVERTER_TARGET_FILE(TAROutput.class),

    PLUGINS(null);

    private Class outputClass;
    private Class outputConfigClass;

    OutputTypes(Class outputClass) {
        this.outputClass = outputClass;
        outputConfigClass = null;
    }

    public Class getOutputClass() {
        return outputClass;
    }

    public Class getOutputConfigClass() {
        return outputConfigClass;
    }


    public static OutputTypes parse(String name) {
        OutputTypes calcType;

        try {
            calcType = OutputTypes.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            calcType = parsePlugins(name);
            if (calcType == null) {
                LoggerFactory.getLogger(OutputTypes.class).error("OutputTypes.parse(name:{}) is failed! {}", name, ex.getMessage());
            }
        }

        return calcType;
    }

    public String getName() {
        if (pluginName == null) {
            return this.name();
        }
        return this.pluginName;
    }

    /* for plugins */

    private String pluginName;
    private static HashMap<String, Class> plugins = new HashMap<>();
    private static HashMap<String, Class> pluginConfigs = new HashMap<>();

    public static HashMap<String, Class> getPlugins() {
        return plugins;
    }

    public static HashMap<String, Class> getPluginConfigs() {
        return pluginConfigs;
    }

    public void forPlugins(String name, Class pluginsClass, Class pluginConfigClass) {
        this.outputClass = pluginsClass;
        this.outputConfigClass = pluginConfigClass;
        this.pluginName = name;
    }


    private static OutputTypes parsePlugins(String name) {
        Class pluginsClass = plugins.get(name);
        if (pluginsClass == null) {
            return null;
        }

        Class pluginConfigClass = pluginConfigs.get(name);
        if (pluginConfigClass == null) {
            return null;
        }

        OutputTypes outputTypes = OutputTypes.PLUGINS;
        outputTypes.forPlugins(name, pluginsClass, pluginConfigClass);
        return outputTypes;
    }

    public static void addPlugins(String outputName, String outputClassName) throws ClassNotFoundException {
        Class outputClass;
        outputClass = Class.forName(outputClassName);
        plugins.put(outputName, outputClass);

        Class outputConfigClass;
        String outputConfigClassName = outputClassName.concat("Config");
        outputConfigClass = Class.forName(outputConfigClassName);
        pluginConfigs.put(outputName, outputConfigClass);
    }

    @Override
    public String toString() {
        return getName();
    }

}
