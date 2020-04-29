package com.clevel.dconvers.format;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.dynvalue.LUPValue;
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
import java.util.*;

public class MarkdownFormatter extends DataFormatter {

    private List<Integer> columnWidth;
    private boolean needHeader;
    private int rowIndex;
    private int rowIndexWidth;
    private String eol;
    private String eof;
    private String remarkEOL;

    private boolean showTitle;
    private boolean showRowNumber;
    private boolean needMermaid;
    private boolean mermaidFullStack;

    public MarkdownFormatter(DConvers dconvers, String name, String eol, String eof, boolean showTitle, boolean showRowNumber, boolean mermaid, boolean mermaidFullStack) {
        super(dconvers, name, true);

        this.eol = eol;
        this.eof = eof;
        remarkEOL = "<br/>";
        outputType = "markdown";
        this.showTitle = showTitle;
        this.showRowNumber = showRowNumber;
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

        if (showTitle) {
            return eol + "# TABLE: " + dataTable.getName().replaceAll("[_]", " ").toUpperCase() + "<br/><sup><sup>(" + dataTable.getName() + ")</sup></sup>" + eol;
        }

        return null;
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
        String value;
        int columnIndex;
        int columnType;
        int valueLength;
        int width;

        StringBuilder record = new StringBuilder();
        if (showRowNumber) {
            record.append("| ").append(rowNumber).append(StringUtils.repeat(' ', rowIndexWidth - rowNumber.length() - 1));
        }

        if (needHeader) {
            needHeader = false;

            String headerSeparator;
            String header;
            String name;
            int nameLength;

            if (showRowNumber) {
                headerSeparator = "|-" + StringUtils.repeat('-', rowIndexWidth - 2) + ":";
                header = "| No." + StringUtils.repeat(' ', rowIndexWidth - 4);
            } else {
                headerSeparator = "";
                header = "";
            }

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

                if (Types.INTEGER == columnType || Types.BIGINT == columnType) {
                    headerSeparator += "|" + StringUtils.repeat('-', width - 1) + ":";
                    record.append("| ").append(StringUtils.repeat(' ', width - valueLength - 2)).append(value).append(" ");
                } else if (Types.DECIMAL == columnType) {
                    if (value.equals("0")) {
                        value = "0.0";
                        valueLength = value.length();
                    }
                    headerSeparator += "|" + StringUtils.repeat('-', width - 1) + ":";
                    record.append("| ").append(StringUtils.repeat(' ', width - valueLength - 2)).append(value).append(" ");
                } else if (Types.DATE == columnType) {
                    headerSeparator += "|:" + StringUtils.repeat('-', width - 2) + ":";
                    record.append("| ").append(value).append(StringUtils.repeat(' ', width - valueLength - 2)).append(" ");
                } else {
                    headerSeparator += "|" + StringUtils.repeat('-', width);
                    record.append("| ").append(value).append(StringUtils.repeat(' ', width - valueLength - 2)).append(" ");
                }

                columnIndex++;
            }

            header += "|" + eol;
            headerSeparator += "|" + eol;
            record.append("|").append(eol);

            return header + headerSeparator + record.toString();

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

            if (Types.INTEGER == columnType || Types.BIGINT == columnType) {
                record.append("| ").append(StringUtils.repeat(' ', width - valueLength - 2)).append(value).append(" ");
            } else if (Types.DECIMAL == columnType) {
                if (value.equals("0")) {
                    value = "0.0";
                    valueLength = value.length();
                }
                record.append("| ").append(StringUtils.repeat(' ', width - valueLength - 2)).append(value).append(" ");
            } else {
                record.append("| ").append(value).append(StringUtils.repeat(' ', width - valueLength - 2)).append(" ");
            }

            columnIndex++;
        }
        record.append("|").append(eol);

        return record.toString();
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

    static private class Identifier {
        DataTable dataTable;
        String name;
        String identifier;
        String remark;

        public Identifier(DataTable dataTable) {
            this.dataTable = dataTable;
            this.remark = null;
        }

        public int compareTo(Identifier another) {
            return this.identifier.compareTo(another.identifier);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Identifier that = (Identifier) o;
            return identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }
    }

    private class Mermaid {
        private List<DataTable> registeredDataTableList;
        List<String> pointerList;

        HashMap<String, Identifier> dataSourceMap;
        HashMap<String, Identifier> sourceMap;
        HashMap<String, Identifier> targetMap;
        HashMap<String, Identifier> mappingMap;
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
            DataSource dataSource = dconvers.getDataSource(dataSourceName);
            if (dataSource == null || dataSource.getDataSourceConfig().getDbms() == null) {
                dsName += "<br/>" + query;
            } else {
                dsName += "<br/>from " + getTableName(query).toLowerCase();
            }

            Identifier identifier = dataSourceMap.get(dsName);
            if (identifier != null) {
                return identifier;
            }

            identifier = new Identifier(null);
            identifier.name = dsName;
            identifier.identifier = Prefix.DATASOURCE.identifierPrefix + (dataSourceMap.size() + 1);
            dataSourceMap.put(dsName, identifier);

            return identifier;
        }

        Identifier prepareDataTable(DataTable dataTable) {
            Identifier identifier = null;
            String dataTableName;
            String dataTableIdentifier = "";
            String remark = "";

            if (dataTable == null) {
                identifier = new Identifier(null);
                identifier.name = "NULL";
                identifier.identifier = "null";
                return identifier;
            }

            dataTableName = dataTable.getName().toUpperCase();
            switch (dataTable.getTableType()) {
                case SRC:
                    dataTableName = Prefix.SRC.namePrefix + dataTableName;
                    identifier = sourceMap.get(dataTableName);
                    if (identifier == null) {
                        identifier = new Identifier(dataTable);
                        identifier.name = dataTableName;
                        identifier.identifier = Prefix.SRC.identifierPrefix + (sourceMap.size() + 1);
                        sourceMap.put(dataTableName, identifier);
                    }
                    break;

                case TAR:
                    dataTableName = Prefix.TAR.namePrefix + dataTableName;
                    identifier = targetMap.get(dataTableName);
                    if (identifier == null) {
                        identifier = new Identifier(dataTable);
                        identifier.name = dataTableName;
                        identifier.identifier = Prefix.TAR.identifierPrefix + (targetMap.size() + 1);
                        targetMap.put(dataTableName, identifier);
                    }
                    Target target = (Target) dataTable.getOwner();
                    int transformCount = target.getTargetConfig().getTransformConfig().getTransformList().size();
                    if (transformCount > 0) {
                        identifier.remark = "transform x" + transformCount;
                    }
                    break;

                default: //case MAP:
                    dataTableName = Prefix.MAP.namePrefix + dataTableName;
                    identifier = mappingMap.get(dataTableName);
                    if (identifier == null) {
                        identifier = new Identifier(dataTable);
                        identifier.name = dataTableName;
                        identifier.identifier = Prefix.MAP.identifierPrefix + (mappingMap.size() + 1);
                        mappingMap.put(dataTableName, identifier);
                    }
                    break;
            }

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
            if (dataTable == null || isRegistered(dataTable)) {
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

                    Converter converter = dconvers.currentConverter;
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
                        remark = "|" + dataTable.getIdColumnName() + remarkEOL + "target_id|";
                        addPointer(targetDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);

                        dataTablePair = (Pair<DataTable, DataTable>) mappingTable.getOwner();
                        if (dataTablePair == null) {
                            log.warn("Markdown.mermaid: dataTablePair is null for mappingTable({}) in target({}), owner({})", mappingDataTableIdentifier.name, targetDataTableIdentifier.name, mappingTable.getOwner());
                            continue;
                        }

                        /*source to mapping*/
                        DataTable asSourceDataTable = dataTablePair.getKey();
                        sourceDataTableIdentifier = prepareDataTable(asSourceDataTable);
                        remark = "|" + asSourceDataTable.getIdColumnName() + remarkEOL + "source_id|";
                        addPointer(sourceDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);
                    } // end for

                    /*source of columns to target*/
                    List<DynamicValue> dynamicValueList = target.getDynamicValueList();
                    if (dynamicValueList == null) {
                        break;
                    }
                    LUPValue lookupValue;
                    DataTable lookupTable;
                    Identifier lookupTableIdentifier;
                    for (DynamicValue dynamicValue : dynamicValueList) {
                        if (!DynamicValueType.LUP.equals(dynamicValue.getDynamicValueType())) {
                            continue;
                        }

                        lookupValue = (LUPValue) dynamicValue;
                        lookupTable = lookupValue.getLookupTable();
                        lookupTableIdentifier = prepareDataTable(lookupTable);

                        remark = "|" + lookupValue.getValueColumnName() + remarkEOL + lookupValue.getName() + "|";
                        addPointer(lookupTableIdentifier.identifier + pointer + remark + targetDataTableIdentifier.identifier);

                        if (fullStack) {
                            registerDataTable(lookupTable);
                        }
                    }
                } // end case TAR:
                break;

                default:/*case MAP:*/ {
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

                    String remark = "|" + sourceDataTableIdentifier.dataTable.getIdColumnName() + remarkEOL + "source_id|";
                    addPointer(sourceDataTableIdentifier.identifier + pointer + remark + mappingDataTableIdentifier.identifier);

                    remark = "|" + targetDataTableIdentifier.dataTable.getIdColumnName() + remarkEOL + "target_id|";
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
            //log.debug("getTableName.query='{}'", query);

            int fromIndex = query.indexOf("SELECT");
            if (fromIndex < 0) {
                return "Unknown";
            }

            fromIndex = query.indexOf("FROM", fromIndex);
            if (fromIndex < 0) {
                return "Procedure";
            }
            fromIndex += 4;

            String subQuery;
            int toIndex = query.indexOf("\n", fromIndex);
            if (toIndex < 0) {
                subQuery = query.substring(fromIndex);
            } else {
                subQuery = query.substring(fromIndex, toIndex);
            }
            subQuery = subQuery.trim();

            toIndex = subQuery.indexOf(" ", fromIndex);
            if (toIndex < 0) {
                toIndex = subQuery.length();
            }
            //log.debug("getTableName.subQuery='{}', fronIndex({}) toIndex({})", subQuery, fromIndex, toIndex);

            String tableName = subQuery.substring(0, toIndex);
            tableName = tableName.replaceAll("[{}()<>+*/\\-]", "").trim();
            //log.debug("getTableName.tableName='{}' after replaced", tableName);

            if (tableName.length() == 0) {
                return "Complex-SQL";
            }

            return tableName;
        }
    } // end class Mermaid

    private String generateMermaid(Mermaid mermaid) {
        StringBuilder classes = new StringBuilder();
        StringBuilder generated = new StringBuilder();
        generated.append(eol).append("#### ").append(mermaid.name).append(eol).append(eol)
                .append("```mermaid").append(eol)
                .append("graph TD;").append(eol)
                .append("classDef source fill:pink,stroke:red,stroke-width:4px;").append(eol)
                .append("classDef map fill:chocolate,stroke:red,stroke-width:4px;").append(eol)
                .append("classDef target fill:yellow,stroke:black,stroke-width:4px;").append(eol);

        if (mermaid.dataSourceMap.size() > 0) {
            generated.append(eol);
            classes.append("class ");

            generateMermaidFromMap(generated, classes, mermaid.dataSourceMap);
            classes.deleteCharAt(classes.length() - 1).append(" datasource;");
        }

        if (mermaid.sourceMap.size() > 0) {
            generated.append(eol);
            classes.append(eol).append("class ");

            generateMermaidFromMap(generated, classes, mermaid.sourceMap);
            classes.deleteCharAt(classes.length() - 1).append(" source;");
        }

        if (mermaid.targetMap.size() > 0) {
            generated.append(eol);
            classes.append(eol).append("class ");

            generateMermaidFromMap(generated, classes, mermaid.targetMap);
            classes.deleteCharAt(classes.length() - 1).append(" target;");
        }

        if (mermaid.mappingMap.size() > 0) {
            generated.append(eol);
            classes.append(eol).append("class ");

            generateMermaidFromMap(generated, classes, mermaid.mappingMap);
            classes.deleteCharAt(classes.length() - 1).append(" map;");
        }

        if (mermaid.pointerList.size() > 0) {
            generated.append(eol);

            mermaid.pointerList.sort(Comparator.naturalOrder());
            for (String pair : mermaid.pointerList) {
                generated.append(pair).append(";").append(eol);
            }
        }

        generated.append(eol).append(classes).append(eol).append("```").append(eol).append(eof);
        return generated.toString();
    }

    private void generateMermaidFromMap(StringBuilder generated, StringBuilder classes, HashMap<String, Identifier> map) {
        List<Identifier> identifierList = new ArrayList<>(map.values());
        identifierList.sort(Identifier::compareTo);

        for (Identifier identifier : identifierList) {
            generated.append(identifier.identifier).append("(").append(identifier.name);
            if (identifier.remark != null) {
                generated.append("<br/>").append(identifier.remark);
            }
            generated.append(");").append(eol);
            classes.append(identifier.identifier).append(",");
        }
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(MarkdownFormatter.class);
    }

}
