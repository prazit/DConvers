package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataDate;
import com.clevel.dconvers.ngin.data.DataLong;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.*;
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
    private List<Source> sortedSource;

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
        sortedSource = new ArrayList<>();
        for (SourceConfig sourceConfig : sourceConfigMap.values()) {
            name = sourceConfig.getName();
            source = new Source(application, name, this, sourceConfig);
            valid = source.isValid();
            if (!valid) {
                return false;
            }
            sourceMap.put(name, source);
            sortedSource.add(source);
        }
        sortedSource.sort((o1, o2) -> o1.getSourceConfig().getIndex() > o2.getSourceConfig().getIndex() ? 1 : -1);

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

        log.info("Build {} source tables", sortedSource.size());
        for (Source source : sortedSource) {
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
        boolean success = true;

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

            if (printTargetTable) {
                dataTable = target.getDataTable();
                targetFileNumber.increaseValueBy(1);
                outputFile = outputTargetPath + "V" + targetFileNumber.getValue() + "__" + targetConfig.getOutput();
                headPrint = "--\n"
                        + "-- Generated by dconvers at " + nowString + ".\n"
                        + "-- This sql file contains " + dataTable.getRowCount() + " rows from target(" + target.getName() + ") in converter(" + name + ")\n"
                        + "-- Data from : source(" + targetConfig.getSource() + ")\n"
                        + "--\n";
                success = success && printDataTableToSQL(headPrint, dataTable, outputFile, charset, sqlCreateFormatter, sqlInsertFormatter, sqlUpdateFormatter);

                if (targetConfig.isMarkdown()) {
                    headPrint = headPrint.replaceAll("--", ">").replaceAll("\n", "  \n");
                    outputFile = outputFile.substring(0, outputFile.length() - 4) + ".md";
                    success = success && printDataTableToMarkdown(headPrint, dataTable, outputFile, charset);
                }
            }

            // -- Start Mapping File

            if (printMappingTable) {
                dataTable = target.getMappingTable();
                mappingFileNumber.increaseValueBy(1);
                outputFile = outputMappingPath + "V" + mappingFileNumber.getValue() + "__" + target.getMappingTable().getTableName() + ".sql";
                headPrint = "--\n"
                        + "-- Generated by dconvers at " + nowString + ".\n"
                        + "-- This sql file contains " + dataTable.getRowCount() + " rows from mapping between source(" + targetConfig.getSource() + ") and target(" + target.getName() + ") in converter(" + name + ")\n"
                        + "--\n";
                success = success && printDataTableToSQL(headPrint, dataTable, outputFile, charset, sqlCreate, sqlInsertFormatter, sqlUpdateFormatter);
            }

        }

        if (!printSourceTable) {
            return success;
        }

        SourceConfig sourceConfig;
        for (Source source : sortedSource) {
            log.trace("print Source({}) ...", source.getName());

            sourceConfig = source.getSourceConfig();
            if (sourceConfig.isInsert()) {
                sqlInsertFormatter = sqlInsert;
            } else {
                sqlInsertFormatter = null;
            }
            if (sourceConfig.isCreate()) {
                sqlCreateFormatter = sqlCreate;
            } else {
                sqlCreateFormatter = null;
            }

            dataTable = source.getDataTable();
            String query = dataTable.getQuery();
            if (query.endsWith("." + Property.SQL.key())) {
                continue;
            }

            // -- Start Source File
            sourceFileNumber.increaseValueBy(1);
            outputFile = outputSourcePath + "V" + sourceFileNumber.getValue() + "__" + source.getSourceConfig().getOutput();
            headPrint = "--\n"
                    + "-- Generated by dconvers at " + nowString + ".\n"
                    + "-- This sql file contains " + dataTable.getRowCount() + " rows from source(" + source.getName() + ") in converter(" + name + ")\n"
                    + "-- Data from : " + query + "\n"
                    + "--\n";
            success = success && printDataTableToSQL(headPrint, dataTable, outputFile, charset, sqlCreateFormatter, sqlInsertFormatter, null);

            if (sourceConfig.isMarkdown()) {
                headPrint = headPrint.replaceAll("--", ">").replaceAll("\n", "  \n");
                outputFile = outputFile.substring(0, outputFile.length() - 4) + ".md";
                success = success && printDataTableToMarkdown(headPrint, dataTable, outputFile, charset);
            }
        }

        return success;
    }

    private boolean printDataTableToSQL(String headPrint, DataTable dataTable, String outputFile, String charset, DataFormatter sqlCreate, DataFormatter sqlInsert, DataFormatter sqlUpdate) {
        String tableName = dataTable.getTableName();

        Writer writer = createFileWithHeadPrint(headPrint, tableName, outputFile, charset);
        if (writer == null) {
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
            log.error("Close file(" + outputFile + ") is failed", e);
            return false;
        }

        return true;
    }

    private boolean printDataTableToMarkdown(String headPrint, DataTable dataTable, String outputFile, String charset) {
        String tableName = dataTable.getTableName();

        Writer writer = createFileWithHeadPrint(headPrint, tableName, outputFile, charset);
        if (writer == null) {
            return false;
        }

        MarkdownFormatter markdown = new MarkdownFormatter();
        markdown.print(dataTable, writer);

        try {
            writer.close();
        } catch (IOException e) {
            log.error("Close file(" + outputFile + ") is failed", e);
            return false;
        }

        return true;
    }

    private Writer createFileWithHeadPrint(String headPrint, String tableName, String outputFile, String charset) {
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
                log.error("System.out is not ready, try again later, ", e);
                writer = null;
            }
        }

        try {
            writer.write(headPrint);
        } catch (IOException e) {
            log.error("write head print is failed, ", e);
            writer = null;
        }

        return writer;
    }

    public ConverterConfigFile getConverterConfigFile() {
        return converterConfigFile;
    }

    public Source getSource(String name) {
        if (name == null) {
            log.debug("getSource({}) return null", name);
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