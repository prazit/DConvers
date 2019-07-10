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

    OutputTypes(Class outputClass) {
        this.outputClass = outputClass;
    }

    public Class getOutputClass() {
        return outputClass;
    }


    public static OutputTypes parse(String name) {
        OutputTypes calcType;

        try {
            name = name.toUpperCase();
            calcType = OutputTypes.valueOf(name);
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

    public void forPlugins(String name, Class pluginsClass) {
        this.outputClass = pluginsClass;
        this.pluginName = name;
    }


    private static OutputTypes parsePlugins(String name) {
        name = name.toUpperCase();
        Class pluginsClass = plugins.get(name);
        if (pluginsClass == null) {
            return null;
        }

        OutputTypes calcTypes = OutputTypes.PLUGINS;
        calcTypes.forPlugins(name, pluginsClass);
        return calcTypes;
    }

    public static void addPlugins(String outputName, String outputClassName) throws ClassNotFoundException {
        Class outputClass;
        outputClass = Class.forName(outputClassName);
        plugins.put(outputName.toUpperCase(), outputClass);
    }

    @Override
    public String toString() {
        return getName();
    }

}
