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
import com.clevel.dconvers.input.DataSource;
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
            mermaid.registerDataTable(dataTable);
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
        private List<DataTable> registeredDataTableList;
        List<String> pointerList;

        HashMap<String, String> dataSourceMap;
        HashMap<String, String> sourceMap;
        HashMap<String, String> targetMap;
        HashMap<String, String> mappingMap;
        boolean fullStack;
        String name;

        Mermaid(boolean fullStack) {
            registeredDataTableList = new ArrayList<>();
            dataSourceMap = new HashMap<>();
            sourceMap = new HashMap<>();
            targetMap = new HashMap<>();
            mappingMap = new HashMap<>();
            pointerList = new ArrayList<>();
            this.fullStack = fullStack;
        }

        Identifier prepareDataSource(String dataSourceName, String query) {
            String dsName = dataSourceName.contains(":") ? dataSourceName : Prefix.DATASOURCE.namePrefix + dataSourceName;
            DataSource dataSource = application.getDataSource(dataSourceName);
            if (dataSource.getDataSourceConfig().getDbms() == null) {
                dsName += "<br/>" + query;
            } else {
                dsName += "<br/>from " + getTableName(query).toLowerCase();
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

        void addPointer(String pointerDef) {
            if (!pointerList.contains(pointerDef)) {
                pointerList.add(pointerDef);
            }
        }

        boolean isRegistered(DataTable dataTable) {
            return registeredDataTableList.contains(dataTable);
        }

        void registerDataTable(DataTable dataTable) {
            if (isRegistered(dataTable)) {
                return;
            }

            registeredDataTableList.add(dataTable);
            String pointer = "-->";

            DynamicValueType tableType = dataTable.getTableType();
            switch (tableType) {
                case SRC: {
                    Identifier sourceDataTableIdentifier = prepareDataTable(dataTable);
                    if (registeredDataTableList.size() == 1) {
                        name = sourceDataTableIdentifier.name;
                    }

                    Source source = (Source) dataTable.getOwner();
                    SourceConfig sourceConfig = source.getSourceConfig();
                    String query = sourceConfig.getQuery();
                    String dataSourceName = sourceConfig.getDataSource().toUpperCase();
                    Identifier dataSourceIdentifier = prepareDataSource(dataSourceName, query);

                    String remark = "|" + dataTable.getRowCount() + " rows|";
                    addPointer(dataSourceIdentifier.identifier + pointer + remark + sourceDataTableIdentifier.identifier);
                }
                break;

                case TAR: {
                    Identifier targetDataTableIdentifier = prepareDataTable(dataTable);
                    if (registeredDataTableList.size() == 1) {
                        name = targetDataTableIdentifier.name;
                    }

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

                        sourceDataTableIdentifier = prepareDataTable(sourceDataTable);
                        remark = "|" + sourceDataTable.getRowCount() + " rows|";
                        addPointer(sourceDataTableIdentifier.identifier + pointer + remark + targetDataTableIdentifier.identifier);

                        if (fullStack) {
                            registerDataTable(sourceDataTable);
                        }
                    }

                    /*both source and target to mapping*/
                    Pair<DataTable, DataTable> dataTablePair;
                    Identifier mappingDataTableIdentifier;
                    for (DataTable mappingTable : target.getMappingTableList()) {
                        mappingDataTableIdentifier = prepareDataTable(mappingTable);

                        /*current target to mapping*/
                        remark = "|" + dataTable.getIdColumnName() + "<br/><br/><br/>" + "target_id|";
                        addPointer(targetDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);

                        dataTablePair = (Pair<DataTable, DataTable>) mappingTable.getOwner();
                        if (dataTablePair == null) {
                            log.warn("Markdown.mermaid: dataTablePair is null for mappingTable({}) in target({}), owner({})", mappingDataTableIdentifier.name, targetDataTableIdentifier.name, mappingTable.getOwner());
                            continue;
                        }

                        /*source to mapping*/
                        DataTable asSourceDataTable = dataTablePair.getKey();
                        sourceDataTableIdentifier = prepareDataTable(asSourceDataTable);
                        remark = "|" + asSourceDataTable.getIdColumnName() + "<br/><br/><br/>" + "source_id|";
                        addPointer(sourceDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);
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
                        lookupTableIdentifier = prepareDataTable(lookupTable);

                        remark = "|" + colValue.getValueColumnName() + "<br/><br/><br/>" + colValue.getName() + "|";
                        addPointer(lookupTableIdentifier.identifier + pointer + remark + targetDataTableIdentifier.identifier);

                        if (fullStack) {
                            registerDataTable(lookupTable);
                        }
                    }
                } // end case TAR:
                break;

                case MAP: {
                    Identifier mappingDataTableIdentifier = prepareDataTable(dataTable);
                    if (registeredDataTableList.size() == 1) {
                        name = mappingDataTableIdentifier.name;
                    }

                    Pair<DataTable, DataTable> dataTablePair = (Pair<DataTable, DataTable>) dataTable.getOwner();
                    if (dataTablePair == null) {
                        log.warn("Markdown.mermaid: dataTablePair is null for mappingTable({}), owner({})", name, dataTable.getOwner());
                        break;
                    }

                    Identifier sourceDataTableIdentifier = prepareDataTable(dataTablePair.getKey());
                    Identifier targetDataTableIdentifier = prepareDataTable(dataTablePair.getValue());

                    String remark = "|" + sourceDataTableIdentifier.dataTable.getIdColumnName() + "<br/><br/><br/>" + "source_id|";
                    addPointer(sourceDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);

                    remark = "|" + targetDataTableIdentifier.dataTable.getIdColumnName() + "<br/><br/><br/>" + "target_id|";
                    addPointer(targetDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);

                    if (fullStack) {
                        registerDataTable(sourceDataTableIdentifier.dataTable);
                        registerDataTable(targetDataTableIdentifier.dataTable);
                    }
                }
                break;
            }
        }

        private String getTableName(String query) {
            query = query.toUpperCase();
            int fromIndex = query.indexOf("FROM ");
            if (fromIndex < 0) {
                return "Procedure";
            }
            fromIndex += 5;

            int blankIndex = query.indexOf(" ", fromIndex);
            if (blankIndex < 0) {
                blankIndex = query.indexOf("\n", fromIndex);
                if (blankIndex < 0) {
                    return "Unknown";
                }
            }

            String tableName = query.substring(fromIndex, blankIndex).trim();
            tableName = tableName.replaceAll("[()<>]", "").trim();

            if (tableName.length() == 0) {
                return "Nested SQL";
            }

            return tableName;
        }
    } // end class Mermaid

    private String generateMermaid(Mermaid mermaid) {
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
            generated.append(eol);
            classes.append("class ");

            String sourceName;
            String sourceIdentifier;
            for (Map.Entry<String, String> sourceEntry : mermaid.sourceMap.entrySet()) {
                sourceName = sourceEntry.getKey();
                sourceIdentifier = sourceEntry.getValue();
                generated.append(sourceIdentifier).append("(").append(sourceName).append(");").append(eol);
                classes.append(sourceIdentifier).append(",");
            }

            classes.deleteCharAt(classes.length() - 1).append(" source;");
        }

        if (mermaid.targetMap.size() > 0) {
            generated.append(eol);
            classes.append(eol).append("class ");

            String targetName;
            String targetIdentifier;
            for (Map.Entry<String, String> targetEntry : mermaid.targetMap.entrySet()) {
                targetName = targetEntry.getKey();
                targetIdentifier = targetEntry.getValue();
                generated.append(targetIdentifier).append("(").append(targetName).append(");").append(eol);
                classes.append(targetIdentifier).append(",");
            }

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
