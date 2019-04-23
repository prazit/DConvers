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

    public MarkdownFormatter(Application application, String name, String eol, String eof, boolean mermaid) {
        super(application, name, true);

        this.eol = eol;
        this.eof = eof;
        outputType = "markdown";
        this.needMermaid = mermaid;
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
        return (needMermaid) ? generateMermaid(dataTable) : eof;
    }

    private String generateMermaid(DataTable dataTable) {

        /* Prepare */

        String srcPrefix = DynamicValueType.SRC.name() + ":";
        String tarPrefix = DynamicValueType.TAR.name() + ":";
        String mapPrefix = DynamicValueType.MAP.name() + ":";
        String pointer = "-->";

        List<String> dataSourceList = new ArrayList<>();
        HashMap<String, String> sourceMap = new HashMap<>();
        HashMap<String, String> targetMap = new HashMap<>();
        HashMap<String, String> mappingMap = new HashMap<>();
        List<String> pointerList = new ArrayList<>();

        DynamicValueType tableType = dataTable.getTableType();
        switch (tableType) {
            case SRC: {
                Source source = (Source) dataTable.getOwner();
                String dataSourceName = source.getSourceConfig().getDataSource();
                String sourceName = source.getName();
                if (!sourceName.startsWith(srcPrefix)) {
                    sourceName = srcPrefix + sourceName;
                }
                dataSourceList.add(dataSourceName);
                sourceMap.put(sourceName, "src" + (sourceMap.size() + 1));
                pointerList.add(dataSourceName + pointer + "src" + sourceMap.size());
            }
            break;

            case TAR: {
                Target target = (Target) dataTable.getOwner();
                String targetName = tarPrefix + target.getName();
                targetMap.put(targetName, "tar" + (targetMap.size() + 1));
                String targetIdentifier = "tar" + targetMap.size();

                /*source to target*/
                for (String sourceName : target.getTargetConfig().getSourceList()) {
                    if (sourceName.startsWith(srcPrefix)) {
                        sourceMap.put(sourceName, "src" + (sourceMap.size() + 1));
                        pointerList.add("src" + sourceMap.size() + pointer + targetIdentifier);
                    } else if (sourceName.startsWith(tarPrefix)) {
                        targetMap.put(sourceName, "tar" + (targetMap.size() + 1));
                        pointerList.add("tar" + targetMap.size() + pointer + targetIdentifier);
                    } else if (sourceName.startsWith(mapPrefix)) {
                        mappingMap.put(sourceName, "map" + (mappingMap.size() + 1));
                        pointerList.add("map" + mappingMap.size() + pointer + targetIdentifier);
                    } else {
                        sourceMap.put(srcPrefix + sourceName, "src" + (sourceMap.size() + 1));
                        pointerList.add("src" + sourceMap.size() + pointer + targetIdentifier);
                    }
                }

                /*source/target to mapping*/
                Pair<DataTable, DataTable> dataTablePair;
                String mappingName;
                for (DataTable mappingTable : target.getMappingTableList()) {
                    mappingName = mappingTable.getName();
                    mappingMap.put(mapPrefix + mappingName, "map" + (mappingMap.size() + 1));
                    pointerList.add(targetIdentifier + pointer + "map" + mappingMap.size());

                    dataTablePair = (Pair<DataTable, DataTable>) mappingTable.getOwner();
                    if (dataTablePair == null) {
                        log.warn("Markdown.mermaid: dataTablePair is null for mappingTable({}) in target({}), owner({})", mappingName, targetIdentifier, mappingTable.getOwner());
                    } else {
                        String srcName = srcPrefix + dataTablePair.getKey().getName().toUpperCase();
                        String srcIdentifier = sourceMap.get(srcName);
                        if (srcIdentifier == null) {
                            srcIdentifier = "src" + sourceMap.size() + 1;
                            sourceMap.put(srcName, srcIdentifier);
                        }
                        pointerList.add(srcIdentifier + pointer + "map" + mappingMap.size());
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
                                    lookupTableIdentifier = sourceMap.get(lookupTableName);
                                    if (lookupTableIdentifier == null) {
                                        lookupTableIdentifier = "src" + (sourceMap.size() + 1);
                                        sourceMap.put(lookupTableName, lookupTableIdentifier);
                                    }
                                    pointerList.add(lookupTableIdentifier + pointer + targetIdentifier);
                                    break;

                                case TAR:
                                    lookupTableName = tarPrefix + lookupTableName;
                                    lookupTableIdentifier = targetMap.get(lookupTableName);
                                    if (lookupTableIdentifier == null) {
                                        lookupTableIdentifier = "tar" + (targetMap.size() + 1);
                                        targetMap.put(lookupTableName, lookupTableIdentifier);
                                    }
                                    pointerList.add(lookupTableIdentifier + pointer + targetIdentifier);
                                    break;

                                case MAP:
                                    lookupTableName = mapPrefix + lookupTableName;
                                    lookupTableIdentifier = mappingMap.get(lookupTableName);
                                    if (lookupTableIdentifier == null) {
                                        lookupTableIdentifier = "map" + (mappingMap.size() + 1);
                                        mappingMap.put(lookupTableName, lookupTableIdentifier);
                                    }
                                    break;

                                default:
                                    continue;
                            }
                            pointerList.add(lookupTableIdentifier + pointer + targetIdentifier);
                        } // end if
                    } // end for
                }
            } // end case TAR:
            break;
        }

        /* Generate */

        int number;
        StringBuilder classes = new StringBuilder();
        StringBuilder generated = new StringBuilder();
        generated.append(eol).append("#### Mermaid Graph").append(eol).append(eol).append("```mermaid").append(eol).append("graph TD;").append(eol).append("classDef source fill:pink,stroke:red,stroke-width:4px;").append(eol).append("classDef map fill:chocolate,stroke:red,stroke-width:4px;").append(eol).append("classDef target fill:yellow,stroke:black,stroke-width:4px;").append(eol);

        if (dataSourceList.size() > 0) {
            generated.append(eol);
            number = 0;
            for (String dataSourceName : dataSourceList) {
                number++;
                generated.append("datasource").append(number).append("(DataSource:").append(dataSourceName).append(");").append(eol);
            }
        }

        if (sourceMap.size() > 0) {
            generated.append(eol).append("subgraph SOURCES").append(eol);
            classes.append(eol).append("class ");

            String sourceName;
            String sourceIdentifier;
            for (Map.Entry<String, String> sourceEntry : sourceMap.entrySet()) {
                sourceName = sourceEntry.getKey();
                sourceIdentifier = sourceEntry.getValue();
                generated.append(sourceIdentifier).append("(").append(sourceName).append(");").append(eol);
                classes.append(sourceIdentifier).append(",");
            }

            generated.append("end").append(eol);
            classes.deleteCharAt(classes.length() - 1).append(" source;");
        }

        if (targetMap.size() > 0) {
            generated.append(eol).append("subgraph TARGETS").append(eol);
            classes.append(eol).append("class ");

            String targetName;
            String targetIdentifier;
            for (Map.Entry<String, String> targetEntry : targetMap.entrySet()) {
                targetName = targetEntry.getKey();
                targetIdentifier = targetEntry.getValue();
                generated.append(targetIdentifier).append("(").append(targetName).append(");").append(eol);
                classes.append(targetIdentifier).append(",");
            }

            generated.append("end").append(eol);
            classes.deleteCharAt(classes.length() - 1).append(" target;");
        }

        if (mappingMap.size() > 0) {
            generated.append(eol);
            classes.append(eol).append("class ");

            String mapName;
            String mapIdentifier;
            for (Map.Entry<String, String> mappingEntry : mappingMap.entrySet()) {
                mapName = mappingEntry.getKey();
                mapIdentifier = mappingEntry.getValue();
                generated.append(mapIdentifier).append("(").append(mapName).append(");").append(eol);
                classes.append(mapIdentifier).append(",");
            }

            classes.deleteCharAt(classes.length() - 1).append(" map;");
        }

        if (pointerList.size() > 0) {
            generated.append(eol);
            for (String pair : pointerList) {
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
