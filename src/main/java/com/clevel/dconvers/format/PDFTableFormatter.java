package com.clevel.dconvers.format;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class PDFTableFormatter extends DataFormatter {

    private String pdfFileName;

    private boolean useJrxmlFilename;
    private String jrxmlFileName;
    private InputStream jrxmlInputStream;

    private class JRDataTable extends DataTable implements JRDataSource {

        private boolean needHeader;
        private int currentRow;
        private List<DataColumn> columnList;

        public JRDataTable(DConvers dconvers, DataTable dataTable) {
            super(dconvers, dataTable.getName(), dataTable.getIdColumnName());
            currentRow = -1;
            needHeader = true;
            setOwner(dataTable.getOwner());
            setQuery(dataTable.getQuery());
            setDataSource(dataTable.getDataSource());
            setMetaData(dataTable.getMetaData());
            setRowList(dataTable.getRowList());
        }

        @Override
        public boolean next() throws JRException {
            int rowCount = getRowCount();
            //log.debug("next of currentRow({}), rowcount({})", currentRow, rowCount);
            if (rowCount == 0) {
                return false;
            }

            if (needHeader) {
                columnList = getRow(0).getColumnList();
                if (currentRow < 0) {
                    currentRow++;
                } else {
                    needHeader = false;
                }
                return true;
            }

            if (currentRow >= rowCount) {
                columnList = getRow(0).getColumnList();
                return false;
            }

            currentRow++;
            columnList = getRow(currentRow).getColumnList();
            return true;
        }

        /**
         * @param jrField name of field in jrxml file (row=0 is column headers) (row>0 is column values)
         * @return
         * @throws JRException
         */
        @Override
        public Object getFieldValue(JRField jrField) throws JRException {
            // field1,field2,...
            String fieldName = jrField.getName();
            int columnIndex = Integer.parseInt(fieldName.substring(5)) - 1;
            //log.debug("getFieldValue(name:{}, desc:{}, class:{}) columnIndex is {}", fieldName, jrField.getDescription(), jrField.getValueClassName(), columnIndex);

            if (columnIndex >= columnList.size()) {
                return "-";
            }

            DataColumn dataColumn = columnList.get(columnIndex);
            if (needHeader) {
                return dataColumn.getName();
            }
            return dataColumn.getValue();
        }
    }

    public PDFTableFormatter(DConvers dconvers, String name, String pdfFileName, Object jrxml) {
        super(dconvers, name, false);

        this.pdfFileName = pdfFileName;
        outputType = "PDF Table";

        useJrxmlFilename = jrxml instanceof String;
        if (useJrxmlFilename) {
            jrxmlFileName = (String) jrxml;
        } else {
            jrxmlInputStream = (InputStream) jrxml;
        }
    }

    @Override
    protected String preFormat(DataTable dataTable) {

        try {

            JasperReport jasperReport;
            if (useJrxmlFilename) {
                jasperReport = JasperCompileManager.compileReport(jrxmlFileName);
            } else {
                jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);
            }

            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("TITLE", "Table: " + dataTable.getName());
            parameters.put("QUERY", "Query: " + dataTable.getQuery());

            JRDataTable jrDataTable = new JRDataTable(dconvers, dataTable);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jrDataTable);
            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFileName);

        } catch (Exception ex) {
            error("PDFTableFormatter Error: ", ex);
        }

        return null;

    }

    @Override
    public String format(DataRow row) {
        return null;
    }


    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(PDFTableFormatter.class);
    }
}
