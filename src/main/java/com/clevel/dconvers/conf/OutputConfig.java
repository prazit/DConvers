package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.output.OutputTypes;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OutputConfig extends Config {

    private boolean sql;
    private String sqlOutput;
    private boolean sqlOutputAppend;
    private String sqlOutputCharset;
    private String sqlOutputEOL;
    private String sqlTable;
    private String sqlNameQuotes;
    private String sqlValueQuotes;
    private boolean sqlCreate;
    private boolean sqlInsert;
    private boolean sqlUpdate;
    private List<String> sqlPostSQL;
    private List<String> sqlPreSQL;


    private boolean markdown;
    private String markdownOutput;
    private boolean markdownOutputAppend;
    private String markdownOutputCharset;
    private String markdownOutputEOL;


    private boolean pdf;
    private Object pdfJRXML;
    private String pdfOutput;


    private boolean txt;
    private String txtOutput;
    private boolean txtOutputAppend;
    private String txtOutputCharset;
    private String txtOutputEOL;
    private String txtSeparator;
    private String txtFormat;
    private String txtFormatDate;
    private String txtFormatDatetime;
    private String txtFillString;
    private String txtFillNumber;
    private String txtFillDate;


    private boolean csv;
    private String csvOutput;
    private boolean csvOutputAppend;
    private String csvOutputCharset;
    private String csvOutputEOL;
    private String csvSeparator;


    private boolean dbInsert;
    private String dbInsertDataSource;
    private String dbInsertTable;
    private String dbInsertNameQuotes;
    private String dbInsertValueQuotes;
    private List<String> dbInsertPostSQL;
    private List<String> dbInsertPreSQL;


    private boolean dbUpdate;
    private String dbUpdateDataSource;
    private String dbUpdateTable;
    private String dbUpdateId;
    private String dbUpdateNameQuotes;
    private String dbUpdateValueQuotes;
    private List<String> dbUpdatePostSQL;
    private List<String> dbUpdatePreSQL;


    private List<OutputTypes> outputTypeList;

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
        outputTypeList = new ArrayList<>();

        // Defaults Properties for SQL
        sql = false;
        sqlOutput = baseProperty + ".sql";
        sqlOutputAppend = false;
        sqlOutputCharset = "UTF-8";
        sqlOutputEOL = "\n";
        sqlTable = name;
        sqlNameQuotes = "";
        sqlValueQuotes = "\"";
        sqlCreate = false;
        sqlInsert = false;
        sqlUpdate = false;
        sqlPreSQL = new ArrayList<>();
        sqlPostSQL = new ArrayList<>();

        String key = Property.SQL.prefixKey(baseProperty);
        sql = properties.getBoolean(key, sql);
        if (sql) {
            outputTypeList.add(OutputTypes.SQL_FILE);

            Configuration sqlProperties = properties.subset(key);
            sqlOutput = sqlProperties.getString(Property.OUTPUT_FILE.key(), sqlOutput);
            sqlOutputAppend = sqlProperties.getBoolean(Property.OUTPUT_APPEND.key(), sqlOutputAppend);
            sqlOutputCharset = sqlProperties.getString(Property.OUTPUT_CHARSET.key(), sqlOutputCharset);
            sqlOutputEOL = sqlProperties.getString(Property.OUTPUT_EOL.key(), sqlOutputEOL);
            sqlTable = sqlProperties.getString(Property.TABLE.key(), sqlTable);
            sqlNameQuotes = sqlProperties.getString(Property.QUOTES.connectKey(Property.NAME), sqlNameQuotes);
            sqlValueQuotes = sqlProperties.getString(Property.QUOTES.connectKey(Property.VALUE), sqlValueQuotes);
            sqlCreate = sqlProperties.getBoolean(Property.CREATE.key(), sqlCreate);
            sqlInsert = sqlProperties.getBoolean(Property.INSERT.key(), sqlInsert);
            sqlUpdate = sqlProperties.getBoolean(Property.UPDATE.key(), sqlUpdate);
            sqlPreSQL = getStringList(sqlProperties, Property.PRE_SQL.key());
            sqlPostSQL = getStringList(sqlProperties, Property.POST_SQL.key());
        }

        // Default Properties for Markdown
        markdown = false;
        markdownOutput = baseProperty + ".md";
        markdownOutputAppend = false;
        markdownOutputCharset = "UTF-8";
        markdownOutputEOL = "\n";

        key =  Property.MARKDOWN.prefixKey(baseProperty);
        markdown = properties.getBoolean(key, markdown);
        if (markdown) {
            outputTypeList.add(OutputTypes.MARKDOWN_FILE);

            Configuration markdownProperties = properties.subset(key);
            markdownOutput = markdownProperties.getString(Property.OUTPUT_FILE.key(), markdownOutput);
            markdownOutputAppend = markdownProperties.getBoolean(Property.OUTPUT_APPEND.key(), markdownOutputAppend);
            markdownOutputCharset = markdownProperties.getString(Property.OUTPUT_CHARSET.key(), markdownOutputCharset);
            markdownOutputEOL = markdownProperties.getString(Property.OUTPUT_EOL.key(), markdownOutputEOL);
        }

        // Default Properties for PDF
        pdf = false;
        pdfOutput = baseProperty + ".md";
        pdfJRXML = "";

        key = Property.PDF_TABLE.prefixKey(baseProperty);
        pdf = properties.getBoolean(key, pdf);
        if (pdf) {
            outputTypeList.add(OutputTypes.PDF_FILE);

            Configuration pdfProperties = properties.subset(key);
            pdfOutput = pdfProperties.getString(Property.OUTPUT_FILE.key(), pdfOutput);
            String jrxml = pdfProperties.getString(Property.OUTPUT_FILE.key(), (String) pdfJRXML);
            if (jrxml.isEmpty()) {
                pdfJRXML = getDefaultJRXML();
            } else {
                pdfJRXML = jrxml;
            }
        }

        // TXT Output Properties
        txt = false;
        txtOutput = baseProperty + ".txt";
        txtOutputAppend = false;
        txtOutputCharset = "UTF-8";
        txtOutputEOL = "\n";
        txtSeparator = "";
        txtFormat = "STR:80";
        txtFormatDate = "YYYYMMdd";
        txtFormatDatetime = "YYYYMMddHHmmss";
        txtFillString = " ";
        txtFillNumber = "0";
        txtFillDate = " ";

        key = Property.TXT.prefixKey(baseProperty);
        txt = properties.getBoolean(key, txt);
        if (txt) {
            outputTypeList.add(OutputTypes.TXT_FILE);

            Configuration txtProperties = properties.subset(key);
            txtOutput = txtProperties.getString(Property.OUTPUT_FILE.key(), txtOutput);
            txtOutputAppend = txtProperties.getBoolean(Property.OUTPUT_APPEND.key(), txtOutputAppend);
            txtOutputCharset = txtProperties.getString(Property.OUTPUT_CHARSET.key(), txtOutputCharset);
            txtOutputEOL = txtProperties.getString(Property.OUTPUT_EOL.key(), txtOutputEOL);
            txtSeparator = txtProperties.getString(Property.SEPARATOR.key(), txtSeparator);
            txtFormat = txtProperties.getString(Property.FORMAT.key(), txtFormat);
            txtFormatDate = txtProperties.getString(Property.FORMAT_DATE.key(), txtFormatDate);
            txtFormatDatetime = txtProperties.getString(Property.FORMAT_DATETIME.key(), txtFormatDatetime);
            txtFillString = txtProperties.getString(Property.FILL_STRING.key(), txtFillString);
            txtFillNumber = txtProperties.getString(Property.FILL_NUMBER.key(), txtFillNumber);
            txtFillDate = txtProperties.getString(Property.FILL_DATE.key(), txtFillDate);
        }

        // CSV Output Properties
        csv = false;
        csvOutput = baseProperty + ".txt";
        csvOutputAppend = false;
        csvOutputCharset = "UTF-8";
        csvOutputEOL = "\n";
        csvSeparator = ",";

        key = Property.CSV.prefixKey(baseProperty);
        csv = properties.getBoolean(key, csv);
        if (csv) {
            outputTypeList.add(OutputTypes.CSV_FILE);

            Configuration csvProperties = properties.subset(key);
            csvOutput = csvProperties.getString(Property.OUTPUT_FILE.key(), csvOutput);
            csvOutputAppend = csvProperties.getBoolean(Property.OUTPUT_APPEND.key(), csvOutputAppend);
            csvOutputCharset = csvProperties.getString(Property.OUTPUT_CHARSET.key(), csvOutputCharset);
            csvOutputEOL = csvProperties.getString(Property.OUTPUT_EOL.key(), csvOutputEOL);
            csvSeparator = csvProperties.getString(Property.SEPARATOR.key(), csvSeparator);
        }

        // DBInsert Output Properties
        dbInsert = false;
        dbInsertDataSource = "";
        dbInsertTable = name;
        dbInsertNameQuotes = "";
        dbInsertValueQuotes = "\"";
        dbInsertPreSQL = new ArrayList<>();
        dbInsertPostSQL = new ArrayList<>();

        key = Property.DBINSERT.prefixKey(baseProperty);
        dbInsert = properties.getBoolean(key, dbInsert);
        if (dbInsert) {
            outputTypeList.add(OutputTypes.INSERT_DB);

            Configuration dbInsertProperties = properties.subset(key);
            dbInsertDataSource = dbInsertProperties.getString(Property.DATA_SOURCE.key(), dbInsertDataSource);
            dbInsertTable = dbInsertProperties.getString(Property.TABLE.key(), dbInsertTable);
            dbInsertNameQuotes = dbInsertProperties.getString(Property.QUOTES.connectKey(Property.NAME), dbInsertNameQuotes);
            dbInsertValueQuotes = dbInsertProperties.getString(Property.QUOTES.connectKey(Property.VALUE), dbInsertValueQuotes);
            dbInsertPreSQL = getStringList(dbInsertProperties, Property.PRE_SQL.key());
            dbInsertPostSQL = getStringList(dbInsertProperties, Property.POST_SQL.key());
        }

        // DBUpdate Output Properties
        dbUpdate = false;
        dbUpdateDataSource = "";
        dbUpdateTable = name;
        dbUpdateNameQuotes = "";
        dbUpdateValueQuotes = "\"";
        dbUpdateId = properties.getString(Property.ID.key(), "id");
        dbUpdatePreSQL = new ArrayList<>();
        dbUpdatePostSQL = new ArrayList<>();

        key = Property.DBUPDATE.prefixKey(baseProperty);
        dbUpdate = properties.getBoolean(key, dbUpdate);
        if (dbUpdate) {
            outputTypeList.add(OutputTypes.UPDATE_DB);

            Configuration dbUpdateProperties = properties.subset(key);
            dbUpdateDataSource = dbUpdateProperties.getString(Property.DATA_SOURCE.key(), dbUpdateDataSource);
            dbUpdateTable = dbUpdateProperties.getString(Property.TABLE.key(), dbUpdateTable);
            dbUpdateId = dbUpdateProperties.getString(Property.ID.key(), dbUpdateId);
            dbUpdateNameQuotes = dbUpdateProperties.getString(Property.QUOTES.connectKey(Property.NAME), dbUpdateNameQuotes);
            dbUpdateValueQuotes = dbUpdateProperties.getString(Property.QUOTES.connectKey(Property.VALUE), dbUpdateValueQuotes);
            dbUpdatePreSQL = getStringList(dbUpdateProperties, Property.PRE_SQL.key());
            dbUpdatePostSQL = getStringList(dbUpdateProperties, Property.POST_SQL.key());
        }

        return true;
    }

    private List<String> getStringList(Configuration properties, String key) {
        List<Object> objectList;
        try {
            objectList = properties.getList(key);
        } catch (ConversionException ex) {
            objectList = new ArrayList<>();
        }

        List<String> stringList = new ArrayList<>();
        for (Object obj : objectList) {
            stringList.add(obj.toString());
        }
        return stringList;
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

    public String getSqlOutputCharset() {
        return sqlOutputCharset;
    }

    public String getSqlOutputEOL() {
        return sqlOutputEOL;
    }

    public String getSqlTable() {
        return sqlTable;
    }

    public String getSqlNameQuotes() {
        return sqlNameQuotes;
    }

    public String getSqlValueQuotes() {
        return sqlValueQuotes;
    }

    public boolean isSqlCreate() {
        return sqlCreate;
    }

    public boolean isSqlInsert() {
        return sqlInsert;
    }

    public boolean isSqlUpdate() {
        return sqlUpdate;
    }

    public List<String> getSqlPostSQL() {
        return sqlPostSQL;
    }

    public List<String> getSqlPreSQL() {
        return sqlPreSQL;
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

    public String getMarkdownOutputCharset() {
        return markdownOutputCharset;
    }

    public String getMarkdownOutputEOL() {
        return markdownOutputEOL;
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

    public String getTxtOutputCharset() {
        return txtOutputCharset;
    }

    public String getTxtOutputEOL() {
        return txtOutputEOL;
    }

    public String getTxtSeparator() {
        return txtSeparator;
    }

    public String getTxtFormat() {
        return txtFormat;
    }

    public String getTxtFormatDate() {
        return txtFormatDate;
    }

    public String getTxtFormatDatetime() {
        return txtFormatDatetime;
    }

    public String getTxtFillString() {
        return txtFillString;
    }

    public String getTxtFillNumber() {
        return txtFillNumber;
    }

    public String getTxtFillDate() {
        return txtFillDate;
    }

    public boolean isCsv() {
        return csv;
    }

    public String getCsvOutput() {
        return csvOutput;
    }

    public String getCsvSeparator() {
        return csvSeparator;
    }

    public boolean isCsvOutputAppend() {
        return csvOutputAppend;
    }

    public String getCsvOutputCharset() {
        return csvOutputCharset;
    }

    public String getCsvOutputEOL() {
        return csvOutputEOL;
    }

    public boolean isDbInsert() {
        return dbInsert;
    }

    public String getDbInsertDataSource() {
        return dbInsertDataSource;
    }

    public String getDbInsertTable() {
        return dbInsertTable;
    }

    public String getDbInsertNameQuotes() {
        return dbInsertNameQuotes;
    }

    public String getDbInsertValueQuotes() {
        return dbInsertValueQuotes;
    }

    public List<String> getDbInsertPostSQL() {
        return dbInsertPostSQL;
    }

    public List<String> getDbInsertPreSQL() {
        return dbInsertPreSQL;
    }

    public boolean isDbUpdate() {
        return dbUpdate;
    }

    public String getDbUpdateDataSource() {
        return dbUpdateDataSource;
    }

    public String getDbUpdateTable() {
        return dbUpdateTable;
    }

    public String getDbUpdateId() {
        return dbUpdateId;
    }

    public String getDbUpdateNameQuotes() {
        return dbUpdateNameQuotes;
    }

    public String getDbUpdateValueQuotes() {
        return dbUpdateValueQuotes;
    }

    public List<String> getDbUpdatePostSQL() {
        return dbUpdatePostSQL;
    }

    public List<String> getDbUpdatePreSQL() {
        return dbUpdatePreSQL;
    }

    public List<OutputTypes> getOutputTypeList() {
        return outputTypeList;
    }

}