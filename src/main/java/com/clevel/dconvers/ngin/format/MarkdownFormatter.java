package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.conf.Defaults;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.apache.commons.lang3.StringUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MarkdownFormatter extends DataFormatter {

    private List<Integer> columnWidth;
    private boolean needHeader;
    private int rowIndex;
    private int rowIndexWidth;

    public MarkdownFormatter() {
        super(true);

        outputType = "markdown";
    }

    @Override
    protected String preFormat(DataTable dataTable) {
        columnWidth = new ArrayList<>();
        needHeader = true;
        rowIndex = 0;
        rowIndexWidth = 5;

        List<DataRow> rowList = dataTable.getAllRow();
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

        return "\n\n# TABLE: " + dataTable.getTableName().replaceAll("[_]", " ").toUpperCase() + "<br/><sup><sup>(" + dataTable.getTableName() + ")</sup></sup>\n\n";
    }

    private void registerColumnWidth(int columnIndex, String value) {
        Integer width;
        int valueLength;

        if (value == null) {
            valueLength = 2;
        } else {
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
    protected String format(DataRow row) {
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

            String headerSeparator = "|:" + StringUtils.repeat('-', rowIndexWidth - 2) + ":";
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
                valueLength = value == null ? 4 : value.length();
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

            header += "|\n";
            headerSeparator += "|\n";
            record += "|\n";

            return header + headerSeparator + record;

        } // end of if(needHeader)


        columnIndex = 0;
        for (DataColumn column : columnList) {
            value = column.getValue();
            valueLength = value == null ? 4 : value.length();
            width = columnWidth.get(columnIndex);

            columnType = column.getType();
            if (Types.INTEGER == columnType || Types.BIGINT == columnType || Types.DECIMAL == columnType) {
                record += "| " + StringUtils.repeat(' ', width - valueLength - 2) + value + " ";
            } else {
                record += "| " + value + StringUtils.repeat(' ', width - valueLength - 2) + " ";
            }

            columnIndex++;
        }
        record += "|\n";

        return record;
    }


    @Override
    protected String postFormat(DataTable dataTable) {
        List<String> postUpdate = dataTable.getPostUpdate();

        if (postUpdate.size() > 0) {
            String lines = "> Post Update:  ";
            for (String sql : postUpdate) {
                lines += "> " + sql + "  \n";
            }
            return lines;
        }

        return null;
    }
}
