package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataLong;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.output.OutputFactory;
import com.clevel.dconvers.ngin.output.OutputTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            log.debug("No source config in converter({}).", name);
        }

        if (targetMap.size() == 0) {
            log.debug("No target config in converter({})", name);
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

    public boolean print() {

        log.trace("Converter({}).print", name);
        boolean success = true;

        Map<SystemVariable, DataColumn> systemVariableMap = application.systemVariableMap;
        DataLong sourceFileNumber = (DataLong) systemVariableMap.get(SystemVariable.SOURCE_FILE_NUMBER);
        DataLong targetFileNumber = (DataLong) systemVariableMap.get(SystemVariable.TARGET_FILE_NUMBER);
        DataLong mappingFileNumber = (DataLong) systemVariableMap.get(SystemVariable.MAPPING_FILE_NUMBER);

        OutputConfig outputConfig;
        DataTable dataTable;

        for (Source source : sortedSource) {
            sourceFileNumber.increaseValueBy(1);

            // -- Outputs for Source Table

            outputConfig = source.getSourceConfig().getOutputConfig();
            dataTable = source.getDataTable();
            for (OutputTypes outputType : outputConfig.getOutputTypeList()) {
                log.trace("printing Source({}) to Output({})", source.getName(), outputType.name());
                if (!OutputFactory.getOutput(application, outputType).print(outputConfig, dataTable)) {
                    return false;
                }
            }

        }

        for (Target target : sortedTarget) {
            targetFileNumber.increaseValueBy(1);
            mappingFileNumber.increaseValueBy(1);

            // -- Outputs for Target Table

            outputConfig = target.getTargetConfig().getOutputConfig();
            dataTable = target.getDataTable();
            for (OutputTypes outputType : outputConfig.getOutputTypeList()) {
                log.trace("printing Target({}) to Output({})", target.getName(), outputType.name());
                if (!OutputFactory.getOutput(application, outputType).print(outputConfig, dataTable)) {
                    return false;
                }
            }

            // -- Outputs for Mapping Table

            /* TODO need output for MappingTable (create new Target.mappingOutputConfig and then uncomment this block)
            outputConfig = target.getTargetConfig().getMappingOutputConfig();
            dataTable = target.getMappingTable();

            for (OutputTypes outputType : mappingOutputConfig.getOutputTypeList()) {
                log.trace("printing Mapping({}) to Output({})", target.getName(), outputType.name());
                OutputFactory.getOutput(outputType).print(outputConfig, dataTable);
            }*/

        }

        return success;
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

    /**
     * @param dataTableMapping example: "SRC:MyTableName"
     * @return Source DataTable which has the specified name, otherwise return null
     */
    public DataTable getDataTable(String dataTableMapping) {
        DataTable dataTable;
        String[] mappings = dataTableMapping.split("[:]");
        DynamicValueType tableType = DynamicValueType.valueOf(mappings[0]);

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