package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OutputConfig extends Config {

    private boolean sql;
    private String sqlOutput;
    private boolean sqlOutputAppend;
    private boolean sqlCreate;
    private boolean sqlInsert;
    private List<String> sqlPostUpdate;

    private boolean markdown;
    private String markdownOutput;
    private boolean markdownOutputAppend;

    private boolean pdf;
    private Object pdfJRXML;
    private String pdfOutput;

    private boolean txt;
    private String txtOutput;
    private boolean txtOutputAppend;
    private String txtSeparator;
    private String txtFormat;
    private List<BigDecimal> txtLengthList;
    private List<String> txtTypeList;

    private boolean csv;
    private String csvOutput;
    private boolean csvOutputAppend;
    private String csvSeparator;

    public OutputConfig(Application application, String baseProperty, Configuration baseProperties) {
        super(application, baseProperty);
        this.properties = baseProperties;

        valid = loadProperties();
        if (valid) valid = validate();

        log.trace("OutputConfig({}) is created", name);
    }

    @Override
    protected boolean loadProperties() {
        String baseProperty = name;
        Configuration baseProperties = properties;
        String table = baseProperties.getString(Property.TABLE.key());

        // Defaults Properties for SQL
        sql = false;
        sqlOutput = table + ".sql";
        sqlOutputAppend = false;
        sqlCreate = false;
        sqlInsert = false;
        sqlPostUpdate = new ArrayList<>();

        String key = baseProperty + "." + Property.SQL.key();
        sql = baseProperties.getBoolean(key, sql);
        if (sql) {
            Configuration sqlProperties = properties.subset(baseProperty + "." + Property.SQL.key());
            sqlOutput = sqlProperties.getString(Property.OUTPUT_FILE.key(), sqlOutput);
            sqlOutputAppend = sqlProperties.getBoolean(Property.OUTPUT_APPEND.key(), sqlOutputAppend);
            sqlCreate = sqlProperties.getBoolean(Property.CREATE.key(), sqlCreate);
            sqlInsert = sqlProperties.getBoolean(Property.INSERT.key(), sqlInsert);

            List<Object> postUpdateObjectList;
            try {
                postUpdateObjectList = sqlProperties.getList(Property.POST_UPDATE.key());
            } catch (ConversionException ex) {
                postUpdateObjectList = new ArrayList<>();
            }
            sqlPostUpdate = new ArrayList<>();
            for (Object obj : postUpdateObjectList) {
                sqlPostUpdate.add(obj.toString());
            }
        }

        // Default Properties for Markdown
        markdown = false;
        markdownOutput = table + ".md";
        markdownOutputAppend = false;

        key = baseProperty + "." + Property.MARKDOWN.key();
        markdown = baseProperties.getBoolean(key, markdown);
        if (markdown) {
            Configuration markdownProperties = properties.subset(baseProperty + "." + Property.MARKDOWN.key());
            markdownOutput = markdownProperties.getString(Property.OUTPUT_FILE.key(), markdownOutput);
            markdownOutputAppend = markdownProperties.getBoolean(Property.OUTPUT_APPEND.key(), markdownOutputAppend);
        }

        // Default Properties for PDF
        pdf = false;
        pdfOutput = table + ".md";
        pdfJRXML = "";

        key = baseProperty + "." + Property.PDF_TABLE.key();
        pdf = baseProperties.getBoolean(key, pdf);
        if (pdf) {
            Configuration pdfProperties = properties.subset(baseProperty + "." + Property.PDF_TABLE.key());
            pdfOutput = pdfProperties.getString(Property.OUTPUT_FILE.key(), pdfOutput);
            String jrxml = pdfProperties.getString(Property.OUTPUT_FILE.key(), "");
            if (jrxml.isEmpty()) {
                pdfJRXML = getDefaultJRXML();
            } else {
                pdfJRXML = jrxml;
            }
        }

        // TXT Output Properties
        txt = false;
        txtOutput = table + ".txt";
        txtOutputAppend = false;
        txtSeparator = "";
        txtFormat = "STR:1024";
        txtLengthList = new ArrayList<>();
        txtTypeList = new ArrayList<>();

        key = baseProperty + "." + Property.MARKDOWN.key();
        txt = baseProperties.getBoolean(key, txt);
        if (txt) {
            Configuration txtProperties = properties.subset(baseProperty + "." + Property.MARKDOWN.key());
            txtOutput = txtProperties.getString(Property.OUTPUT_FILE.key(), txtOutput);
            txtOutputAppend = txtProperties.getBoolean(Property.OUTPUT_APPEND.key(), txtOutputAppend);
            txtSeparator = txtProperties.getString(Property.SEPARATOR.key(), txtSeparator);
            txtFormat = txtProperties.getString(Property.FORMAT.key(), txtFormat);
            extractTxtFormat(txtFormat, txtLengthList, txtTypeList);
        }

        // CSV Output Properties
        csv = false;
        csvOutput = table + ".txt";
        csvOutputAppend = false;
        csvSeparator = ",";

        key = baseProperty + "." + Property.MARKDOWN.key();
        csv = baseProperties.getBoolean(key, csv);
        if (csv) {
            Configuration txtProperties = properties.subset(baseProperty + "." + Property.MARKDOWN.key());
            csvOutput = txtProperties.getString(Property.OUTPUT_FILE.key(), csvOutput);
            csvOutputAppend = txtProperties.getBoolean(Property.OUTPUT_APPEND.key(), csvOutputAppend);
            csvSeparator = txtProperties.getString(Property.SEPARATOR.key(), csvSeparator);
        }

        return true;
    }

    private void extractTxtFormat(String txtFormat, List<BigDecimal> txtLengthList, List<String> txtTypeList) {
        int index = -1;
        txtLengthList.clear();
        txtTypeList.clear();

        String[] columns = txtFormat.split("[,]");
        for (String column : columns) {
            String[] values = column.split("[:]");
            index++;
            txtTypeList.add(index, values[0]);
            txtLengthList.add(index, new BigDecimal(values[1]));
        }
    }

    @Override
    public boolean validate() {
        // TODO: Might be need to validate output configuration
        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(OutputConfig.class);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private InputStream getDefaultJRXML() {
        String jasperName = "/com/clevel/ecm/app/jasper/PDFTable18.jrxml";
        InputStream reportStream = OutputConfig.class.getResourceAsStream(jasperName);
        return reportStream;
    }

    public boolean isSql() {
        return sql;
    }

    public String getSqlOutput() {
        return sqlOutput;
    }

    public boolean isSqlOutputAppend() {
        return sqlOutputAppend;
    }

    public boolean isSqlCreate() {
        return sqlCreate;
    }

    public boolean isSqlInsert() {
        return sqlInsert;
    }

    public List<String> getSqlPostUpdate() {
        return sqlPostUpdate;
    }

    public boolean isMarkdown() {
        return markdown;
    }

    public String getMarkdownOutput() {
        return markdownOutput;
    }

    public boolean isMarkdownOutputAppend() {
        return markdownOutputAppend;
    }

    public boolean isPdf() {
        return pdf;
    }

    public Object getPdfJRXML() {
        return pdfJRXML;
    }

    public String getPdfOutput() {
        return pdfOutput;
    }

    public boolean isTxt() {
        return txt;
    }

    public String getTxtOutput() {
        return txtOutput;
    }

    public boolean isTxtOutputAppend() {
        return txtOutputAppend;
    }

    public String getTxtSeparator() {
        return txtSeparator;
    }

    public String getTxtFormat() {
        return txtFormat;
    }

    public List<BigDecimal> getTxtLengthList() {
        return txtLengthList;
    }

    public List<String> getTxtTypeList() {
        return txtTypeList;
    }

    public boolean isCsv() {
        return csv;
    }

    public String getCsvOutput() {
        return csvOutput;
    }

    public boolean isCsvOutputAppend() {
        return csvOutputAppend;
    }

}