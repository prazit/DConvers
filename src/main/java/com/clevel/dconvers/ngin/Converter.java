package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.data.DataLong;
import com.clevel.dconvers.ngin.data.DataTable;
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
import java.util.*;

public class Converter extends AppBase {

    private ConverterConfigFile converterConfigFile;
    private Map<String, Source> sourceMap;
    private Map<String, Target> targetMap;
    private List<Target> sortedTarget;

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

        Map<String, SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        Map<String, TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();

        boolean valid = true;
        String name;
        Source source;
        sourceMap = new HashMap<>();
        for (SourceConfig sourceConfig : sourceConfigMap.values()) {
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
        sortedTarget = new ArrayList<>();
        for (TargetConfig targetConfig : targetConfigMap.values()) {
            name = targetConfig.getName();
            target = new Target(application, name, this, targetConfig);
            valid = target.isValid();
            if (!valid) {
                return false;
            }
            targetMap.put(name, target);
            sortedTarget.add(target);
        }
        Collections.sort(sortedTarget, (o1, o2) -> o1.getTargetConfig().getIndex() > o2.getTargetConfig().getIndex() ? 1 : -1);

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Converter({}).validate", name);

        if (sourceMap == null || sourceMap.size() == 0) {
            log.error("Sources are required for target({})", name);
            return false;
        }

        if (sourceMap.size() == 0) {
            log.warn("Source is always required to build the target table.");
            application.hasWarning = true;
            return false;
        }

        if (sourceMap.size() == 0) {
            log.warn("Source not found, need one source at least.");
            application.hasWarning = true;
            return false;
        }

        return true;
    }

    public boolean convert() {
        log.trace("Converter({}).convert", name);

        log.info("Build {} source tables", sourceMap.size());
        for (Source source : sourceMap.values()) {
            valid = source.buildDataTable();
            if (!valid) {
                return false;
            }
        }

        log.info("Build {} target tables", sortedTarget.size());
        for (Target target : sortedTarget) {
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

        DataFormatter sqlCreateFormatter;
        DataFormatter sqlInsertFormatter;
        DataFormatter sqlUpdateFormatter;

        TargetConfig targetConfig;
        String charset = "UTF-8";
        String outputFile;
        Writer writer;
        DataLong fileNumber = (DataLong) application.systemVariableMap.get(SystemVariable.FILENUMBER);
        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;

        for (Target target : sortedTarget) {
            log.trace("print Target({}) ...", target.getName());

            targetConfig = target.getTargetConfig();
            if (targetConfig.isInsert()) {
                sqlInsertFormatter = sqlInsert;
                sqlUpdateFormatter = null;
            } else {
                sqlInsertFormatter = null;
                sqlUpdateFormatter = sqlUpdate;
            }
            if (targetConfig.isCreate()) {
                sqlCreateFormatter = sqlCreate;
            } else {
                sqlCreateFormatter = null;
            }

            // -- Start Target File

            fileNumber.increaseValueBy(1);
            outputFile = dataConversionConfigFile.getOutputTargetPath() + "V" + fileNumber.getQuotedValue() + "__" + targetConfig.getOutput();
            printDataTable(target.getDataTable(), outputFile, charset, sqlCreateFormatter, sqlInsertFormatter, sqlUpdateFormatter);

            // -- Start Mapping File

            outputFile = dataConversionConfigFile.getOutputMappingPath() + "V" + fileNumber.getQuotedValue() + "__" + target.getMappingTable().getTableName() + ".sql";
            printDataTable(target.getMappingTable(), outputFile, charset, sqlCreate, sqlInsertFormatter, sqlUpdateFormatter);

        }

        return true;

    }

    private boolean printDataTable(DataTable dataTable, String outputFile, String charset, DataFormatter sqlCreate, DataFormatter sqlInsert, DataFormatter sqlUpdate) {
        String tableName = dataTable.getTableName();

        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), charset);
            log.trace("print DataTable({}) to File({}) ...", tableName, outputFile);
        } catch (Exception e) {
            log.warn("Create output file for '{}' table is failed, {}, print to System.out instead", tableName, e.getMessage());
            application.hasWarning = true;
            return false;
        }

        if (writer == null) {
            try {
                writer = new OutputStreamWriter(System.out, charset);
                log.trace("print DataTable({}) to console ...", tableName);
            } catch (Exception e) {
                log.error("System.out is not ready, {}, try again later", e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        if (sqlCreate != null) {
            sqlCreate.print(dataTable, writer);
        }

        if (sqlInsert != null) {
            sqlInsert.print(dataTable, writer);
        }

        if (sqlUpdate != null) {
            sqlUpdate.print(dataTable, writer);
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public ConverterConfigFile getConverterConfigFile() {
        return converterConfigFile;
    }

    public Source getSource(String name) {
        if (name == null) {
            return null;
        }

        return sourceMap.get(name);
    }

    public Target getTarget(String name) {
        if (name == null) {
            return null;
        }

        return targetMap.get(name);
    }

}