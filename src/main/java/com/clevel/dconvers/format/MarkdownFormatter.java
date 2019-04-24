package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.dynvalue.COLValue;
import com.clevel.dconvers.dynvalue.DynamicValue;
import com.clevel.dconvers.dynvalue.DynamicValueType;
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
            Mermaid mermaid = new Mermaid();
            prepareMermaid(dataTable, mermaid, mermaidFullStack);
            return generateMermaid(mermaid);
        }
        return eof;
    }

    private class Mermaid {
        HashMap<String, String> dataSourceMap;
        HashMap<String, String> sourceMap;
        HashMap<String, String> targetMap;
        HashMap<String, String> mappingMap;
        List<String> pointerList;

        Mermaid() {
            this.dataSourceMap = new HashMap<>();
            this.sourceMap = new HashMap<>();
            this.targetMap = new HashMap<>();
            this.mappingMap = new HashMap<>();
            this.pointerList = new ArrayList<>();
        }
    }

    private void prepareMermaid(DataTable dataTable, Mermaid mermaid, boolean fullStack) {
        String dataSourcePrfix = "DATASOURCE:";
        String srcPrefix = DynamicValueType.SRC.name() + ":";
        String tarPrefix = DynamicValueType.TAR.name() + ":";
        String mapPrefix = DynamicValueType.MAP.name() + ":";
        String pointer = "-->";

        DynamicValueType tableType = dataTable.getTableType();
        switch (tableType) {
            case SRC: {
                Source source = (Source) dataTable.getOwner();
                String dataSourceName = source.getSourceConfig().getDataSource();
                String sourceName = source.getName();
                if (!sourceName.startsWith(srcPrefix)) {
                    sourceName = srcPrefix + sourceName;
                }
                mermaid.dataSourceMap.put(dataSourceName, dataSourcePrfix + (mermaid.dataSourceMap.size() + 1));
                mermaid.sourceMap.put(sourceName, "src" + (mermaid.sourceMap.size() + 1));
                mermaid.pointerList.add(dataSourceName + pointer + "src" + mermaid.sourceMap.size());
            }
            break;

            case TAR: {
                Target target = (Target) dataTable.getOwner();
                String targetName = tarPrefix + target.getName();
                mermaid.targetMap.put(targetName, "tar" + (mermaid.targetMap.size() + 1));
                String targetIdentifier = "tar" + mermaid.targetMap.size();

                /*source to target*/
                for (String sourceName : target.getTargetConfig().getSourceList()) {
                    if (sourceName.startsWith(srcPrefix)) {
                        mermaid.sourceMap.put(sourceName, "src" + (mermaid.sourceMap.size() + 1));
                        mermaid.pointerList.add("src" + mermaid.sourceMap.size() + pointer + targetIdentifier);
                    } else if (sourceName.startsWith(tarPrefix)) {
                        mermaid.targetMap.put(sourceName, "tar" + (mermaid.targetMap.size() + 1));
                        mermaid.pointerList.add("tar" + mermaid.targetMap.size() + pointer + targetIdentifier);
                    } else if (sourceName.startsWith(mapPrefix)) {
                        mermaid.mappingMap.put(sourceName, "map" + (mermaid.mappingMap.size() + 1));
                        mermaid.pointerList.add("map" + mermaid.mappingMap.size() + pointer + targetIdentifier);
                    } else {
                        mermaid.sourceMap.put(srcPrefix + sourceName, "src" + (mermaid.sourceMap.size() + 1));
                        mermaid.pointerList.add("src" + mermaid.sourceMap.size() + pointer + targetIdentifier);
                    }
                }

                /*source/target to mapping*/
                Pair<DataTable, DataTable> dataTablePair;
                String mappingName;
                for (DataTable mappingTable : target.getMappingTableList()) {
                    mappingName = mappingTable.getName();
                    mermaid.mappingMap.put(mapPrefix + mappingName, "map" + (mermaid.mappingMap.size() + 1));
                    mermaid.pointerList.add(targetIdentifier + pointer + "map" + mermaid.mappingMap.size());

                    dataTablePair = (Pair<DataTable, DataTable>) mappingTable.getOwner();
                    if (dataTablePair == null) {
                        log.warn("Markdown.mermaid: dataTablePair is null for mappingTable({}) in target({}), owner({})", mappingName, targetIdentifier, mappingTable.getOwner());
                    } else {
                        String srcName = srcPrefix + dataTablePair.getKey().getName().toUpperCase();
                        String srcIdentifier = mermaid.sourceMap.get(srcName);
                        if (srcIdentifier == null) {
                            srcIdentifier = "src" + mermaid.sourceMap.size() + 1;
                            mermaid.sourceMap.put(srcName, srcIdentifier);
                        }
                        mermaid.pointerList.add(srcIdentifier + pointer + "map" + mermaid.mappingMap.size());
                    }
                }

                /*source of columns to target*/
                List<DynamicValue> dynamicValueList = target.getDynamicValueList();
                if (dynamicValueList != null) {
                    COLValue colValue;
                    DataTable lookupTable;
                    DynamicValueType lookupTableType;
                    String lookupTableName;
                    String lookupTableIdentifier;
                    for (DynamicValue dynamicValue : dynamicValueList) {
                        if (DynamicValueType.COL.equals(dynamicValue.getDynamicValueType())) {
                            colValue = (COLValue) dynamicValue;
                            lookupTable = colValue.getLookupTable();
                            lookupTableType = lookupTable.getTableType();
                            lookupTableName = lookupTable.getName();
                            switch (lookupTableType) {
                                case SRC:
                                    lookupTableName = srcPrefix + lookupTableName;
                                    lookupTableIdentifier = mermaid.sourceMap.get(lookupTableName);
                                    if (lookupTableIdentifier == null) {
                                        lookupTableIdentifier = "src" + (mermaid.sourceMap.size() + 1);
                                        mermaid.sourceMap.put(lookupTableName, lookupTableIdentifier);
                                    }
                                    mermaid.pointerList.add(lookupTableIdentifier + pointer + targetIdentifier);
                                    break;

                                case TAR:
                                    lookupTableName = tarPrefix + lookupTableName;
                                    lookupTableIdentifier = mermaid.targetMap.get(lookupTableName);
                                    if (lookupTableIdentifier == null) {
                                        lookupTableIdentifier = "tar" + (mermaid.targetMap.size() + 1);
                                        mermaid.targetMap.put(lookupTableName, lookupTableIdentifier);
                                    }
                                    mermaid.pointerList.add(lookupTableIdentifier + pointer + targetIdentifier);
                                    break;

                                case MAP:
                                    lookupTableName = mapPrefix + lookupTableName;
                                    lookupTableIdentifier = mermaid.mappingMap.get(lookupTableName);
                                    if (lookupTableIdentifier == null) {
                                        lookupTableIdentifier = "map" + (mermaid.mappingMap.size() + 1);
                                        mermaid.mappingMap.put(lookupTableName, lookupTableIdentifier);
                                    }
                                    break;

                                default:
                                    continue;
                            }
                            mermaid.pointerList.add(lookupTableIdentifier + pointer + targetIdentifier);
                        } // end if
                    } // end for
                }
            } // end case TAR:
            break;
        }
    }

    private String generateMermaid(Mermaid mermaid) {
        int number;
        StringBuilder classes = new StringBuilder();
        StringBuilder generated = new StringBuilder();
        generated.append(eol).append("#### Mermaid Graph").append(eol).append(eol).append("```mermaid").append(eol).append("graph TD;").append(eol).append("classDef source fill:pink,stroke:red,stroke-width:4px;").append(eol).append("classDef map fill:chocolate,stroke:red,stroke-width:4px;").append(eol).append("classDef target fill:yellow,stroke:black,stroke-width:4px;").append(eol);

        if (mermaid.dataSourceMap.size() > 0) {
            generated.append(eol);
            number = 0;
            for (String dataSourceName : mermaid.dataSourceMap.keySet()) {
                number++;
                generated.append("datasource").append(number).append("(DataSource:").append(dataSourceName).append(");").append(eol);
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
