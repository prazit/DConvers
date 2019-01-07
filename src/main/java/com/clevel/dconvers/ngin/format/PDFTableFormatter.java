package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PDFTableFormatter extends DataFormatter {

    private String pdfFileName;

    private boolean useJrxmlFilename;
    private String jrxmlFileName;
    private InputStream jrxmlInputStream;

    public PDFTableFormatter(Application application, String name, String pdfFileName, Object jrxml) {
        super(application, name, false);

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

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataTable);
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
