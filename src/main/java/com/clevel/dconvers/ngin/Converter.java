package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataDate;
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
    private Map<String, DataTable> mappingTableMap;
    private List<Target> sortedTarget;

    private String mappingTablePrefix;

    public Converter(Application application, String name, ConverterConfigFile converterConfigFile) {
        super(application, name);

        this.converterConfigFile = converterConfigFile;
        mappingTablePrefix = application.dataConversionConfigFile.getMappingTablePrefix();

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

        boolean valid;
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
        sortedTarget.sort((o1, o2) -> o1.getTargetConfig().getIndex() > o2.getTargetConfig().getIndex() ? 1 : -1);

        mappingTableMap = new HashMap<>();

        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Converter({}).validate", name);

        if (sourceMap.size() == 0) {
            log.warn("No source in converter({}).", name);
            application.hasWarning = true;
        }

        if (targetMap.size() == 0) {
            log.warn("No target in converter({})", name);
            application.hasWarning = true;
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
        DataTable mapping;
        for (Target target : sortedTarget) {
            valid = target.buildDataTable();
            if (!valid) {
                return false;
            }

            mapping = target.getMappingTable();
            mappingTableMap.put(mapping.getTableName(), mapping);
        }

        return true;
    }

    public boolean print(boolean printSourceTable, boolean printTargetTable, boolean printMappingTable) {
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

        Map<SystemVariable, DataColumn> systemVariableMap = application.systemVariableMap;
        DataLong targetFileNumber = (DataLong) systemVariableMap.get(SystemVariable.TARGET_FILE_NUMBER);
        DataLong mappingFileNumber = (DataLong) systemVariableMap.get(SystemVariable.MAPPING_FILE_NUMBER);
        DataLong sourceFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        DataDate now = (DataDate) systemVariableMap.get(SystemVariable.NOW);
        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;

        String outputTargetPath = dataConversionConfigFile.getOutputTargetPath();
        String outputMappingPath = dataConversionConfigFile.getOutputMappingPath();
        String outputSourcePath = dataConversionConfigFile.getOutputSourcePath();

        String headPrint;
        String nowString = now.getValue();
        DataTable dataTable;

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

            dataTable = target.getDataTable();
            targetFileNumber.increaseValueBy(1);
            outputFile = outputTargetPath + "V" + targetFileNumber.getValue() + "__" + targetConfig.getOutput();
            headPrint = "--\n"
                    + "-- Generated by dconvers at " + nowString + ".\n"
                    + "-- This sql file contains " + dataTable.getRowCount() + " rows from target(" + target.getName() + ") in converter(" + name + ")\n"
                    + "--\n";
            printDataTable(headPrint, dataTable, outputFile, charset, sqlCreateFormatter, sqlInsertFormatter, sqlUpdateFormatter);

            // -- Start Mapping File

            if (!printMappingTable) {
                continue;
            }

            dataTable = target.getMappingTable();
            mappingFileNumber.increaseValueBy(1);
            outputFile = outputMappingPath + "V" + mappingFileNumber.getValue() + "__" + target.getMappingTable().getTableName() + ".sql";
            headPrint = "--\n"
                    + "-- Generated by dconvers at " + nowString + ".\n"
                    + "-- This sql file contains " + dataTable.getRowCount() + " rows from mapping between source(" + target.getTargetConfig().getSource() + ") and target(" + target.getName() + ") in converter(" + name + ")\n"
                    + "--\n";
            printDataTable(headPrint, dataTable, outputFile, charset, sqlCreate, sqlInsertFormatter, sqlUpdateFormatter);
        }

        if (!printSourceTable) {
            return true;
        }

        for (Source source : sourceMap.values()) {
            log.trace("print Source({}) ...", source.getName());

            // -- Start Source File
            dataTable = source.getDataTable();
            sourceFileNumber.increaseValueBy(1);
            outputFile = outputSourcePath + "V" + sourceFileNumber.getValue() + "__" + source.getSourceConfig().getOutput();
            headPrint = "--\n"
                    + "-- Generated by dconvers at " + nowString + ".\n"
                    + "-- This sql file contains " + dataTable.getRowCount() + " rows from source(" + source.getName() + ") in converter(" + name + ")\n"
                    + "--\n";
            printDataTable(headPrint, dataTable, outputFile, charset, sqlCreate, sqlInsert, null);
        }

        return true;

    }

    private boolean printDataTable(String headPrint, DataTable dataTable, String outputFile, String charset, DataFormatter sqlCreate, DataFormatter sqlInsert, DataFormatter sqlUpdate) {
        String tableName = dataTable.getTableName();

        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), charset);
            log.trace("print DataTable({}) to File({}) ...", tableName, outputFile);
        } catch (Exception e) {
            log.warn("Create output file for '{}' table is failed, {}, print to System.out instead", tableName, e.getMessage());
            application.hasWarning = true;
            writer = null;
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

        try {
            writer.write(headPrint);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

    public DataTable getDataTable(String mapping) {
        DataTable dataTable;
        String[] mappings = mapping.split("[:]");
        SourceColumnType tableType = SourceColumnType.valueOf(mappings[0]);

        switch (tableType) {
            case SRC:
                Source source = getSource(mappings[1]);
                if (source == null) {
                    return null;
                }
                return source.getDataTable();

            case TAR:
                Target target = getTarget(mappings[1]);
                if (target == null) {
                    return null;
                }
                return target.getDataTable();

            case MAP:
                dataTable = mappingTableMap.get(mappingTablePrefix + mappings[1]);
                break;

            default:
                dataTable = null;
        }

        return dataTable;
    }
}