package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.ConverterConfigFile;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.conf.TargetConfig;
import com.clevel.dconvers.ngin.data.DataLong;
import com.clevel.dconvers.ngin.format.DataFormatter;
import com.clevel.dconvers.ngin.format.SQLCreateFormatter;
import com.clevel.dconvers.ngin.format.SQLInsertFormatter;
import com.clevel.dconvers.ngin.format.SQLUpdateFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class Converter extends AppBase {

    private ConverterConfigFile converterConfigFile;
    private Map<String, Source> sourceMap;
    private Map<String, Target> targetMap;

    public Converter(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);

        this.converterConfigFile = converterConfigFile;
        valid = prepare();
        if (valid) {
            valid = validate();
        }

        log.trace("Converter({}) is created", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Converter.class);
    }

    private boolean prepare() {
        log.trace("Converter({}).prepare", name);

        Map<String,SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        Map<String,TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();

        boolean valid = true;
        String name;
        Source source;
        sourceMap = new HashMap<>();
        for (SourceConfig sourceConfig:sourceConfigMap.values()) {
            name = sourceConfig.getName();
            source = new Source(application, name, this, sourceConfig);
            valid = source.isValid();
            if (!valid) {
                return false;
            }
            sourceMap.put(name, source);
        }

        Target target;
        targetMap = new HashMap<>();
        for (TargetConfig targetConfig : targetConfigMap.values()) {
            name = targetConfig.getName();
            target = new Target(application, name, this, targetConfig);
            valid = target.isValid();
            if (!valid) {
                return false;
            }
            targetMap.put(name, target);
        }

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Converter({}).validate", name);

        if (sourceMap.size() == 0) {
            log.warn("Source not found, need one source at least.");
            return false;
        }

        if (sourceMap.size() == 0) {
            log.warn("Source not found, need one source at least.");
            return false;
        }

        return true;
    }

    public boolean convert() {
        log.trace("Converter({}).convert", name);


        for (Source source : sourceMap.values()) {
            valid = source.buildDataTable();
            if (!valid) {
                return false;
            }
        }

        for (Target target : targetMap.values()) {
            valid = target.buildDataTable();
            if (!valid) {
                return false;
            }
        }

        return true;
    }

    public boolean print() {
        log.trace("Converter({}).print", name);

        SQLCreateFormatter sqlCreate = new SQLCreateFormatter();
        SQLInsertFormatter sqlInsert = new SQLInsertFormatter();
        SQLUpdateFormatter sqlUpdate = new SQLUpdateFormatter();
        DataFormatter formatter;

        TargetConfig targetConfig;
        String outputFile;

        DataLong fileNumber = (DataLong) application.systemVariableMap.get(SystemVariable.FILENUMBER);

        for (Target target : targetMap.values()) {

            targetConfig = target.getTargetConfig();
            if (targetConfig.isInsert()) {
                formatter = sqlInsert;
            } else {
                formatter = sqlUpdate;
            }

            fileNumber.increaseValueBy(1);
            outputFile = application.dataConversionConfigFile.getConverterRootPath() +"V"+ fileNumber.getValue() +"__"+ targetConfig.getOutput();
            String charset = "UTF-8";
            Writer writer;

            try {
                writer = new OutputStreamWriter(new FileOutputStream(outputFile), charset);
            } catch (Exception e) {
                writer = null;
                log.warn("Create output file is failed, {}", e.getMessage());
            }

            if (writer == null) {
                try {
                    writer = new OutputStreamWriter(System.out, charset);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            if (targetConfig.isCreate()) {
                sqlCreate.print(target.getDataTable(), writer);
            }

            formatter.print(target.getDataTable(), writer);

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            // TODO: create output file for mapping data tables\

        }

        return true;

    }

    public ConverterConfigFile getConverterConfigFile() {
        return converterConfigFile;
    }

    public Map<String, Source> getSourceMap() {
        return sourceMap;
    }

    public Map<String, Target> getTargetMap() {
        return targetMap;
    }
}