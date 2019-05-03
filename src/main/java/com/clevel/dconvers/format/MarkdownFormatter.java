package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.dynvalue.COLValue;
import com.clevel.dconvers.dynvalue.DynamicValue;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.Target;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkdownFormatter extends DataFormatter {

    private List<Integer> columnWidth;
    private boolean needHeader;
    private int rowIndex;
    private int rowIndexWidth;
    private String eol;
    private String eof;
    private boolean needMermaid;
    private boolean mermaidFullStack;

    public MarkdownFormatter(Application application, String name, String eol, String eof, boolean mermaid, boolean mermaidFullStack) {
        super(application, name, true);

        this.eol = eol;
        this.eof = eof;
        outputType = "markdown";
        this.needMermaid = mermaid;
        this.mermaidFullStack = mermaidFullStack;
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        columnWidth = new ArrayList<>();
        needHeader = true;
        rowIndex = 0;
        rowIndexWidth = 5;

        List<DataRow> rowList = dataTable.getRowList();
        if (rowList.size() == 0) {
            return null;
        }

        int rowIndexExpectWidth = String.valueOf(rowList.size()).length() + 2;
        if (rowIndexExpectWidth > rowIndexWidth) {
            rowIndexWidth = rowIndexExpectWidth;
        }

        int columnIndex;
        String value;
        Integer width;

        DataRow firstRow = rowList.get(0);
        columnIndex = 0;
        for (DataColumn column : firstRow.getColumnList()) {
            if (Types.DATE == column.getType()) {
                value = column.getName() + " (" + Defaults.DATE_FORMAT.getStringValue() + ")";
            } else {
                value = column.getName();
            }
            registerColumnWidth(columnIndex, value);
            columnIndex++;
        }

        for (DataRow row : rowList) {
            columnIndex = 0;
            for (DataColumn column : row.getColumnList()) {
                value = column.getValue();
                registerColumnWidth(columnIndex, value);
                columnIndex++;
            }
        }

        return eol + "# TABLE: " + dataTable.getName().replaceAll("[_]", " ").toUpperCase() + "<br/><sup><sup>(" + dataTable.getName() + ")</sup></sup>" + eol;
    }

    private void registerColumnWidth(int columnIndex, String value) {
        Integer width;
        int valueLength;

        if (value == null) {
            valueLength = 2;
        } else {
            value = value.replaceAll("\r\n|\n\r|\n", "<br/>");
            valueLength = value.length() + 2;
        }


        if (columnIndex >= columnWidth.size()) {
            width = 2;
            columnWidth.add(width);
        } else {
            width = columnWidth.get(columnIndex);
        }

        if (valueLength > width) {
            width = valueLength;
            columnWidth.set(columnIndex, width);
        }
    }

    @Override
    public String format(DataRow row) {
        List<DataColumn> columnList = row.getColumnList();
        rowIndex++;

        String rowNumber = String.valueOf(rowIndex);
        String record = "| " + rowNumber + StringUtils.repeat(' ', rowIndexWidth - rowNumber.length() - 1);
        String value;
        int columnIndex;
        int columnType;
        int valueLength;
        int width;

        if (needHeader) {
            needHeader = false;

            String headerSeparator = "|-" + StringUtils.repeat('-', rowIndexWidth - 2) + ":";
            String header = "| No." + StringUtils.repeat(' ', rowIndexWidth - 4);
            String name;
            int nameLength;

            columnIndex = 0;
            for (DataColumn column : columnList) {
                columnType = column.getType();
                if (Types.DATE == columnType) {
                    name = column.getName() + " (" + Defaults.DATE_FORMAT.getStringValue() + ")";
                } else {
                    name = column.getName();
                }
                value = column.getValue();
                nameLength = name.length();
                if (value == null) {
                    valueLength = 4;
                } else {
                    value = value.replaceAll("\r\n|\n\r|\n", "<br/>");
                    value = value.replaceAll("[|]", "&vert;");
                    valueLength = value.length();
                }
                width = columnWidth.get(columnIndex);

                header += "| " + name + StringUtils.repeat(' ', width - nameLength - 2) + " ";

                if (Types.INTEGER == columnType || Types.BIGINT == columnType || Types.DECIMAL == columnType) {
                    headerSeparator += "|" + StringUtils.repeat('-', width - 1) + ":";
                    record += "| " + StringUtils.repeat(' ', width - valueLength - 2) + value + " ";
                } else if (Types.DATE == columnType) {
                    headerSeparator += "|:" + StringUtils.repeat('-', width - 2) + ":";
                    record += "| " + value + StringUtils.repeat(' ', width - valueLength - 2) + " ";
                } else {
                    headerSeparator += "|" + StringUtils.repeat('-', width);
                    record += "| " + value + StringUtils.repeat(' ', width - valueLength - 2) + " ";
                }


                columnIndex++;
            }

            header += "|" + eol;
            headerSeparator += "|" + eol;
            record += "|" + eol;

            return header + headerSeparator + record;

        } // end of if(needHeader)


        columnIndex = 0;
        for (DataColumn column : columnList) {
            columnType = column.getType();
            if (Types.DATE == columnType) {
                value = column.getFormattedValue(Defaults.DATE_FORMAT.getStringValue());
            } else {
                value = column.getValue();
            }

            if (value == null) {
                valueLength = 4;
            } else {
                value = value.replaceAll("\r\n|\n\r|\n", "  ");
                valueLength = value.length();
            }
            width = columnWidth.get(columnIndex);

            if (Types.INTEGER == columnType || Types.BIGINT == columnType || Types.DECIMAL == columnType) {
                record += "| " + StringUtils.repeat(' ', width - valueLength - 2) + value + " ";
            } else {
                record += "| " + value + StringUtils.repeat(' ', width - valueLength - 2) + " ";
            }

            columnIndex++;
        }
        record += "|" + eol;

        return record;
    }

    @Override
    protected String postFormat(DataTable dataTable) {
        if (needMermaid) {
            Mermaid mermaid = new Mermaid(mermaidFullStack);
            prepareMermaid(dataTable, mermaid);
            return generateMermaid(mermaid);
        }
        return eof;
    }

    private enum Prefix {
        DATASOURCE("DATASOURCE:", "datasource"),
        SRC("SRC:", "src"),
        TAR("TAR:", "tar"),
        MAP("MAP:", "map");

        String namePrefix;
        String identifierPrefix;

        Prefix(String namePrefix, String identifierPrefix) {
            this.namePrefix = namePrefix;
            this.identifierPrefix = identifierPrefix;
        }
    }

    private class Identifier {
        DataTable dataTable;
        String name;
        String identifier;

        public Identifier(DataTable dataTable) {
            this.dataTable = dataTable;
        }
    }

    private class Mermaid {
        HashMap<String, String> dataSourceMap;
        HashMap<String, String> sourceMap;
        HashMap<String, String> targetMap;
        HashMap<String, String> mappingMap;
        List<String> pointerList;
        boolean fullStack;
        String name;

        Mermaid(boolean fullStack) {
            this.dataSourceMap = new HashMap<>();
            this.sourceMap = new HashMap<>();
            this.targetMap = new HashMap<>();
            this.mappingMap = new HashMap<>();
            this.pointerList = new ArrayList<>();
            this.fullStack = fullStack;
        }

        Identifier prepareDataSource(String dataSourceName, String query) {
            String dsName = dataSourceName.contains(":") ? dataSourceName : Prefix.DATASOURCE.namePrefix + dataSourceName;
            if (dsName.contains("MARKDOWN") || dsName.contains("SQL")) {
                dsName += "<br/>" + query;
            }

            String dsIdentifier = dataSourceMap.get(dsName);
            if (dsIdentifier == null) {
                dsIdentifier = Prefix.DATASOURCE.identifierPrefix + (dataSourceMap.size() + 1);
                dataSourceMap.put(dsName, dsIdentifier);
            }

            Identifier identifier = new Identifier(null);
            identifier.name = dsName;
            identifier.identifier = dsIdentifier;
            return identifier;
        }

        Identifier prepareDataTable(DataTable dataTable) {
            String dataTableName = dataTable.getName().toUpperCase();
            String dataTableIdentifier = "";
            switch (dataTable.getTableType()) {
                case SRC:
                    dataTableName = Prefix.SRC.namePrefix + dataTableName;
                    dataTableIdentifier = sourceMap.get(dataTableName);
                    if (dataTableIdentifier == null) {
                        dataTableIdentifier = Prefix.SRC.identifierPrefix + (sourceMap.size() + 1);
                        sourceMap.put(dataTableName, dataTableIdentifier);
                    }
                    break;

                case TAR:
                    dataTableName = Prefix.TAR.namePrefix + dataTableName;
                    dataTableIdentifier = targetMap.get(dataTableName);
                    if (dataTableIdentifier == null) {
                        dataTableIdentifier = Prefix.TAR.identifierPrefix + (targetMap.size() + 1);
                        targetMap.put(dataTableName, dataTableIdentifier);
                    }
                    break;

                case MAP:
                    dataTableName = Prefix.MAP.namePrefix + dataTableName;
                    dataTableIdentifier = mappingMap.get(dataTableName);
                    if (dataTableIdentifier == null) {
                        dataTableIdentifier = Prefix.MAP.identifierPrefix + (mappingMap.size() + 1);
                        mappingMap.put(dataTableName, dataTableIdentifier);
                    }
                    break;
            }

            Identifier identifier = new Identifier(dataTable);
            identifier.name = dataTableName;
            identifier.identifier = dataTableIdentifier;
            return identifier;
        }
    } // end class Mermaid

    private void prepareMermaid(DataTable dataTable, Mermaid mermaid) {
        String srcPrefix = DynamicValueType.SRC.name() + ":";
        String tarPrefix = DynamicValueType.TAR.name() + ":";
        String mapPrefix = DynamicValueType.MAP.name() + ":";
        String pointer = "-->";

        DynamicValueType tableType = dataTable.getTableType();
        switch (tableType) {
            case SRC: {
                Identifier sourceDataTableIdentifier = mermaid.prepareDataTable(dataTable);
                mermaid.name = sourceDataTableIdentifier.name;

                Source source = (Source) dataTable.getOwner();
                SourceConfig sourceConfig = source.getSourceConfig();
                Identifier dataSourceIdentifier = mermaid.prepareDataSource(sourceConfig.getDataSource().toUpperCase(), sourceConfig.getQuery());

                String remark = "|" + dataTable.getRowCount() + " rows|";
                mermaid.pointerList.add(dataSourceIdentifier.identifier + pointer + remark + sourceDataTableIdentifier.identifier);
            }
            break;

            case TAR: {
                Identifier targetDataTableIdentifier = mermaid.prepareDataTable(dataTable);
                mermaid.name = targetDataTableIdentifier.name;

                Target target = (Target) dataTable.getOwner();
                String remark;

                Converter converter = application.currentConverter;
                DataTable sourceDataTable;
                Identifier sourceDataTableIdentifier;

                /*source to target*/
                for (String sourceName : target.getTargetConfig().getSourceList()) {
                    sourceName = sourceName.toUpperCase();
                    if (!sourceName.contains(":")) {
                        sourceName = Prefix.SRC.namePrefix + sourceName;
                    }

                    sourceDataTable = converter.getDataTable(sourceName);
                    if (sourceDataTable == null) {
                        log.warn("SourceDataTable({}) is not found!", sourceName);
                        continue;
                    }

                    sourceDataTableIdentifier = mermaid.prepareDataTable(sourceDataTable);
                    remark = "|" + sourceDataTable.getRowCount() + " rows|";
                    mermaid.pointerList.add(sourceDataTableIdentifier.identifier + pointer + remark + targetDataTableIdentifier.identifier);
                }

                /*source/target to mapping*/
                Pair<DataTable, DataTable> dataTablePair;
                Identifier mappingDataTableIdentifier;
                for (DataTable mappingTable : target.getMappingTableList()) {
                    mappingDataTableIdentifier = mermaid.prepareDataTable(mappingTable);

                    /*current target to mapping*/
                    remark = "|" + dataTable.getIdColumnName() + "<br/><br/><br/>" + "target_id|";
                    mermaid.pointerList.add(targetDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);

                    /*source to mapping*/
                    dataTablePair = (Pair<DataTable, DataTable>) mappingTable.getOwner();
                    if (dataTablePair == null) {
                        log.warn("Markdown.mermaid: dataTablePair is null for mappingTable({}) in target({}), owner({})", mappingDataTableIdentifier.name, targetDataTableIdentifier.name, mappingTable.getOwner());
                        continue;
                    }
                    sourceDataTableIdentifier = mermaid.prepareDataTable(dataTablePair.getKey());
                    remark = "|" + dataTablePair.getValue().getIdColumnName() + "<br/><br/><br/>" + "source_id|";
                    mermaid.pointerList.add(sourceDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);
                } // end for

                /*source of columns to target*/
                List<DynamicValue> dynamicValueList = target.getDynamicValueList();
                if (dynamicValueList == null) {
                    break;
                }
                COLValue colValue;
                DataTable lookupTable;
                Identifier lookupTableIdentifier;
                for (DynamicValue dynamicValue : dynamicValueList) {
                    if (!DynamicValueType.COL.equals(dynamicValue.getDynamicValueType())) {
                        continue;
                    }

                    colValue = (COLValue) dynamicValue;
                    lookupTable = colValue.getLookupTable();
                    lookupTableIdentifier = mermaid.prepareDataTable(lookupTable);

                    remark = "|" + colValue.getValueColumnName() + "<br/><br/><br/>" + colValue.getName() + "|";
                    mermaid.pointerList.add(lookupTableIdentifier.identifier + pointer + remark + targetDataTableIdentifier.identifier);
                }
            } // end case TAR:
            break;

            case MAP: {
                Identifier mappingDataTableIdentifier = mermaid.prepareDataTable(dataTable);
                mermaid.name = mappingDataTableIdentifier.name;

                Pair<DataTable, DataTable> dataTablePair = (Pair<DataTable, DataTable>) dataTable.getOwner();
                if (dataTablePair == null) {
                    log.warn("Markdown.mermaid: dataTablePair is null for mappingTable({}), owner({})", mermaid.name, dataTable.getOwner());
                    break;
                }

                Identifier sourceDataTableIdentifier = mermaid.prepareDataTable(dataTablePair.getKey());
                Identifier targetDataTableIdentifier = mermaid.prepareDataTable(dataTablePair.getValue());

                String remark = "|" + sourceDataTableIdentifier.dataTable.getIdColumnName() + "<br/><br/><br/>" + "source_id|";
                mermaid.pointerList.add(sourceDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);

                remark = "|" + targetDataTableIdentifier.dataTable.getIdColumnName() + "<br/><br/><br/>" + "target_id|";
                mermaid.pointerList.add(targetDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);
            }
            break;
        }
    }

    private String generateMermaid(Mermaid mermaid) {
        int number;
        StringBuilder classes = new StringBuilder();
        StringBuilder generated = new StringBuilder();
        generated.append(eol).append("#### ").append(mermaid.name).append(eol).append(eol).append("```mermaid").append(eol).append("graph TD;").append(eol).append("classDef source fill:pink,stroke:red,stroke-width:4px;").append(eol).append("classDef map fill:chocolate,stroke:red,stroke-width:4px;").append(eol).append("classDef target fill:yellow,stroke:black,stroke-width:4px;").append(eol);

        if (mermaid.dataSourceMap.size() > 0) {
            generated.append(eol);
            for (Map.Entry<String, String> datasourceEntry : mermaid.dataSourceMap.entrySet()) {
                generated.append(datasourceEntry.getValue()).append("(").append(datasourceEntry.getKey()).append(");").append(eol);
            }
        }

        if (mermaid.sourceMap.size() > 0) {
            generated.append(eol).append("subgraph SOURCES").append(eol);
            classes.append(eol).append("class ");

            String sourceName;
            String sourceIdentifier;
            for (Map.Entry<String, String> sourceEntry : mermaid.sourceMap.entrySet()) {
                sourceName = sourceEntry.getKey();
                sourceIdentifier = sourceEntry.getValue();
                generated.append(sourceIdentifier).append("(").append(sourceName).append(");").append(eol);
                classes.append(sourceIdentifier).append(",");
            }

            generated.append("end").append(eol);
            classes.deleteCharAt(classes.length() - 1).append(" source;");
        }

        if (mermaid.targetMap.size() > 0) {
            generated.append(eol).append("subgraph TARGETS").append(eol);
            classes.append(eol).append("class ");

            String targetName;
            String targetIdentifier;
            for (Map.Entry<String, String> targetEntry : mermaid.targetMap.entrySet()) {
                targetName = targetEntry.getKey();
                targetIdentifier = targetEntry.getValue();
                generated.append(targetIdentifier).append("(").append(targetName).append(");").append(eol);
                classes.append(targetIdentifier).append(",");
            }

            generated.append("end").append(eol);
            classes.deleteCharAt(classes.length() - 1).append(" target;");
        }

        if (mermaid.mappingMap.size() > 0) {
            generated.append(eol);
            classes.append(eol).append("class ");

            String mapName;
            String mapIdentifier;
            for (Map.Entry<String, String> mappingEntry : mermaid.mappingMap.entrySet()) {
                mapName = mappingEntry.getKey();
                mapIdentifier = mappingEntry.getValue();
                generated.append(mapIdentifier).append("(").append(mapName).append(");").append(eol);
                classes.append(mapIdentifier).append(",");
            }

            classes.deleteCharAt(classes.length() - 1).append(" map;");
        }

        if (mermaid.pointerList.size() > 0) {
            generated.append(eol);
            for (String pair : mermaid.pointerList) {
                generated.append(pair).append(";").append(eol);
            }
        }

        generated.append(eol).append(classes).append(eol).append("```").append(eol).append(eof);
        return generated.toString();
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(MarkdownFormatter.class);
    }

}
