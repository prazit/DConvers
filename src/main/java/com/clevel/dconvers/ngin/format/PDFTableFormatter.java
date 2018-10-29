package com.clevel.dconvers.ngin.format;

import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PDFTableFormatter extends DataFormatter {

    private String pdfFileName;
    private Logger log;

    public PDFTableFormatter(String pdfFileName) {
        super(true);

        this.pdfFileName = pdfFileName;
        outputType = "PDF Table";

        log = LoggerFactory.getLogger(PDFTableFormatter.class);
    }

    @Override
    protected String preFormat(DataTable dataTable) {

        try {

            JasperReport jasperReport = JasperCompileManager.compileReport("C:\\Users\\prazi\\Documents\\GitHub\\Data Conversion\\src\\main\\java\\com\\clevel\\dconvers\\ngin\\format\\PDFTable18.jrxml");

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("TITLE", "Table: " + dataTable.getTableName());
            parameters.put("QUERY", "Query: " + dataTable.getQuery());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataTable);
            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFileName);

        } catch (Exception ex) {
            log.error("PDFTableFormatter Error: ", ex);
        }

        return null;
    }

    @Override
    protected String format(DataRow row) {
        return null;
    }


    @Override
    protected String postFormat(DataTable dataTable) {
        return null;
    }
}
