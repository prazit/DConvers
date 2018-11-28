package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.output.OutputTypes;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OutputConfig extends Config {

    private boolean conf;
    private String confSftp;
    private String confSftpOutput;
    private String confOutput;
    private boolean confOutputAppend;
    private boolean confOutputAutoCreateDir;
    private String confOutputCharset;
    private String confOutputEOL;
    private String confOutputEOF;
    private String confTable;
    private String confColumnName;
    private String confColumnType;
    private String confColumnIsKey;

    private boolean sql;
    private String sqlSftp;
    private String sqlSftpOutput;
    private String sqlOutput;
    private boolean sqlOutputAppend;
    private boolean sqlOutputAutoCreateDir;
    private String sqlOutputCharset;
    private String sqlOutputEOL;
    private String sqlOutputEOF;
    private String sqlTable;
    private String sqlNameQuotes;
    private String sqlValueQuotes;
    private boolean sqlCreate;
    private boolean sqlInsert;
    private boolean sqlUpdate;
    private List<String> sqlPostSQL;
    private List<String> sqlPreSQL;


    private boolean markdown;
    private String markdownSftp;
    private String markdownSftpOutput;
    private String markdownOutput;
    private boolean markdownOutputAppend;
    private boolean markdownOutputAutoCreateDir;
    private String markdownOutputCharset;
    private String markdownOutputEOL;
    private String markdownOutputEOF;


    private boolean pdf;
    private Object pdfJRXML;
    private String pdfSftp;
    private String pdfSftpOutput;
    private String pdfOutput;
    private boolean pdfOutputAutoCreateDir;


    private boolean txt;
    private String txtSftp;
    private String txtSftpOutput;
    private String txtOutput;
    private boolean txtOutputAppend;
    private boolean txtOutputAutoCreateDir;
    private String txtOutputCharset;
    private String txtOutputEOL;
    private String txtOutputEOF;
    private String txtSeparator;
    private String txtFormat;
    private String txtFormatDate;
    private String txtFormatDatetime;
    private String txtFillString;
    private String txtFillNumber;
    private String txtFillDate;


    private boolean csv;
    private String csvSftp;
    private String csvSftpOutput;
    private String csvOutput;
    private boolean csvOutputAppend;
    private boolean csvOutputAutoCreateDir;
    private String csvOutputCharset;
    private String csvOutputEOL;
    private String csvOutputEOF;
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
        sqlSftp = null;
        sqlSftpOutput = null;
        sqlOutput = baseProperty + ".sql";
        sqlOutputAppend = false;
        sqlOutputAutoCreateDir = true;
        sqlOutputCharset = "UTF-8";
        sqlOutputEOL = "\n";
        sqlOutputEOF = "\n";
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
            sqlSftp = sqlProperties.getString(Property.SFTP.key(), sqlSftp);
            sqlSftpOutput = sqlProperties.getString(Property.SFTP.connectKey(Property.OUTPUT_FILE), sqlSftpOutput);
            sqlOutput = sqlProperties.getString(Property.OUTPUT_FILE.key(), sqlOutput);
            sqlOutputAppend = sqlProperties.getBoolean(Property.OUTPUT_APPEND.key(), sqlOutputAppend);
            sqlOutputAutoCreateDir = sqlProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), sqlOutputAutoCreateDir);
            sqlOutputCharset = sqlProperties.getString(Property.OUTPUT_CHARSET.key(), sqlOutputCharset);
            sqlOutputEOL = sqlProperties.getString(Property.OUTPUT_EOL.key(), sqlOutputEOL);
            sqlOutputEOF = sqlProperties.getString(Property.OUTPUT_EOF.key(), sqlOutputEOF);
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
        markdownSftp = null;
        markdownSftpOutput = null;
        markdownOutput = baseProperty + ".md";
        markdownOutputAppend = false;
        markdownOutputAutoCreateDir = true;
        markdownOutputCharset = "UTF-8";
        markdownOutputEOL = "\n";
        markdownOutputEOF = "\n";

        key = Property.MARKDOWN.prefixKey(baseProperty);
        markdown = properties.getBoolean(key, markdown);
        if (markdown) {
            outputTypeList.add(OutputTypes.MARKDOWN_FILE);

            Configuration markdownProperties = properties.subset(key);
            markdownSftp = markdownProperties.getString(Property.SFTP.key(), markdownSftp);
            markdownSftpOutput = markdownProperties.getString(Property.SFTP.connectKey(Property.OUTPUT_FILE), markdownSftpOutput);
            markdownOutput = markdownProperties.getString(Property.OUTPUT_FILE.key(), markdownOutput);
            markdownOutputAppend = markdownProperties.getBoolean(Property.OUTPUT_APPEND.key(), markdownOutputAppend);
            markdownOutputAutoCreateDir = markdownProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), markdownOutputAutoCreateDir);
            markdownOutputCharset = markdownProperties.getString(Property.OUTPUT_CHARSET.key(), markdownOutputCharset);
            markdownOutputEOL = markdownProperties.getString(Property.OUTPUT_EOL.key(), markdownOutputEOL);
            markdownOutputEOF = markdownProperties.getString(Property.OUTPUT_EOF.key(), markdownOutputEOF);
        }

        // Default Properties for PDF
        pdf = false;
        pdfSftp = null;
        pdfSftpOutput = null;
        pdfOutput = baseProperty + ".md";
        pdfOutputAutoCreateDir = true;
        pdfJRXML = "";

        key = Property.PDF_TABLE.prefixKey(baseProperty);
        pdf = properties.getBoolean(key, pdf);
        if (pdf) {
            outputTypeList.add(OutputTypes.PDF_FILE);

            Configuration pdfProperties = properties.subset(key);
            pdfSftp = pdfProperties.getString(Property.SFTP.key(), pdfSftp);
            pdfSftpOutput = pdfProperties.getString(Property.SFTP.connectKey(Property.OUTPUT_FILE), pdfSftpOutput);
            pdfOutput = pdfProperties.getString(Property.OUTPUT_FILE.key(), pdfOutput);
            pdfOutputAutoCreateDir = pdfProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), pdfOutputAutoCreateDir);
            String jrxml = pdfProperties.getString(Property.OUTPUT_FILE.key(), (String) pdfJRXML);
            if (jrxml.isEmpty()) {
                pdfJRXML = getDefaultJRXML();
            } else {
                pdfJRXML = jrxml;
            }
        }

        // TXT Output Properties
        txt = false;
        txtSftp = null;
        txtSftpOutput = null;
        txtOutput = baseProperty + ".txt";
        txtOutputAppend = false;
        txtOutputAutoCreateDir = true;
        txtOutputCharset = "UTF-8";
        txtOutputEOL = "\n";
        txtOutputEOF = "\n";
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
            txtSftp = txtProperties.getString(Property.SFTP.key(), txtSftp);
            txtSftpOutput = txtProperties.getString(Property.SFTP.connectKey(Property.OUTPUT_FILE), txtSftpOutput);
            txtOutput = txtProperties.getString(Property.OUTPUT_FILE.key(), txtOutput);
            txtOutputAppend = txtProperties.getBoolean(Property.OUTPUT_APPEND.key(), txtOutputAppend);
            txtOutputAutoCreateDir = txtProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), txtOutputAutoCreateDir);
            txtOutputCharset = txtProperties.getString(Property.OUTPUT_CHARSET.key(), txtOutputCharset);
            txtOutputEOL = txtProperties.getString(Property.OUTPUT_EOL.key(), txtOutputEOL);
            txtOutputEOF = txtProperties.getString(Property.OUTPUT_EOF.key(), txtOutputEOF);
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
        csvSftp = null;
        csvSftpOutput = null;
        csvOutput = baseProperty + ".txt";
        csvOutputAppend = false;
        csvOutputAutoCreateDir = true;
        csvOutputCharset = "UTF-8";
        csvOutputEOL = "\n";
        csvOutputEOF = "\n";
        csvSeparator = ",";

        key = Property.CSV.prefixKey(baseProperty);
        csv = properties.getBoolean(key, csv);
        if (csv) {
            outputTypeList.add(OutputTypes.CSV_FILE);

            Configuration csvProperties = properties.subset(key);
            csvSftp = csvProperties.getString(Property.SFTP.key(), csvSftp);
            csvSftpOutput = csvProperties.getString(Property.SFTP.connectKey(Property.OUTPUT_FILE), csvSftpOutput);
            csvOutput = csvProperties.getString(Property.OUTPUT_FILE.key(), csvOutput);
            csvOutputAppend = csvProperties.getBoolean(Property.OUTPUT_APPEND.key(), csvOutputAppend);
            csvOutputAutoCreateDir = csvProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), csvOutputAutoCreateDir);
            csvOutputCharset = csvProperties.getString(Property.OUTPUT_CHARSET.key(), csvOutputCharset);
            csvOutputEOL = csvProperties.getString(Property.OUTPUT_EOL.key(), csvOutputEOL);
            csvOutputEOF = csvProperties.getString(Property.OUTPUT_EOF.key(), csvOutputEOF);
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

        // Default Properties for PDF
        conf = false;
        confTable = "TABLE_NAME";           // name of column to use as table name
        confColumnName = "COLUMN_NAME";     // name of column to use as column name
        confColumnType = "COLUMN_TYPE";     // name of column to use as column type
        confColumnIsKey = "IS_KEY";         // name of column to use as column is a primary key
        confOutput = baseProperty + ".conf";
        confOutputAppend = false;
        confOutputAutoCreateDir = true;
        confOutputCharset = "UTF-8";
        confOutputEOL = "\n";
        confOutputEOF = "\n";

        key = Property.CONF.prefixKey(baseProperty);
        conf = properties.getBoolean(key, conf);
        if (conf) {
            outputTypeList.add(OutputTypes.CONFIG_FILE);

            Configuration confProperties = properties.subset(key);
            confTable = confProperties.getString(Property.TABLE.key(), confTable);
            confSftp = confProperties.getString(Property.SFTP.key(), confSftp);
            confSftpOutput = confProperties.getString(Property.SFTP.connectKey(Property.OUTPUT_FILE), confSftpOutput);
            confColumnName = confProperties.getString(Property.TABLE.key(), confColumnName);
            confColumnType = confProperties.getString(Property.TABLE.key(), confColumnType);
            confColumnIsKey = confProperties.getString(Property.TABLE.key(), confColumnIsKey);
            confOutput = confProperties.getString(Property.OUTPUT_FILE.key(), confOutput);
            confOutputAppend = confProperties.getBoolean(Property.OUTPUT_APPEND.key(), confOutputAppend);
            confOutputAutoCreateDir = confProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), confOutputAutoCreateDir);
            confOutputCharset = confProperties.getString(Property.OUTPUT_CHARSET.key(), confOutputCharset);
            confOutputEOL = confProperties.getString(Property.OUTPUT_EOL.key(), confOutputEOL);
            confOutputEOF = confProperties.getString(Property.OUTPUT_EOF.key(), confOutputEOF);
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

    public boolean isConf() {
        return conf;
    }

    public String getConfSftp() {
        return confSftp;
    }

    public String getConfSftpOutput() {
        return confSftpOutput;
    }

    public String getConfTable() {
        return confTable;
    }

    public String getConfColumnName() {
        return confColumnName;
    }

    public String getConfColumnType() {
        return confColumnType;
    }

    public String getConfColumnIsKey() {
        return confColumnIsKey;
    }

    public String getConfOutput() {
        return confOutput;
    }

    public boolean isConfOutputAppend() {
        return confOutputAppend;
    }

    public boolean isConfOutputAutoCreateDir() {
        return confOutputAutoCreateDir;
    }

    public String getConfOutputCharset() {
        return confOutputCharset;
    }

    public String getConfOutputEOL() {
        return application.currentConverter.compileDynamicValues(confOutputEOL);

    }

    public String getConfOutputEOF() {
        return application.currentConverter.compileDynamicValues(confOutputEOF);
    }

    public boolean isSql() {
        return sql;
    }

    public String getSqlSftp() {
        return sqlSftp;
    }

    public String getSqlSftpOutput() {
        return application.currentConverter.compileDynamicValues(sqlSftpOutput);
    }

    public String getSqlOutput() {
        return application.currentConverter.compileDynamicValues(sqlOutput);
    }

    public boolean isSqlOutputAppend() {
        return sqlOutputAppend;
    }

    public boolean isSqlOutputAutoCreateDir() {
        return sqlOutputAutoCreateDir;
    }

    public String getSqlOutputCharset() {
        return sqlOutputCharset;
    }

    public String getSqlOutputEOL() {
        return application.currentConverter.compileDynamicValues(sqlOutputEOL);
    }

    public String getSqlOutputEOF() {
        return application.currentConverter.compileDynamicValues(sqlOutputEOF);
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

    public String getMarkdownSftp() {
        return markdownSftp;
    }

    public String getMarkdownSftpOutput() {
        return application.currentConverter.compileDynamicValues(markdownSftpOutput);
    }

    public String getMarkdownOutput() {
        return application.currentConverter.compileDynamicValues(markdownOutput);
    }

    public boolean isMarkdownOutputAppend() {
        return markdownOutputAppend;
    }

    public boolean isMarkdownOutputAutoCreateDir() {
        return markdownOutputAutoCreateDir;
    }

    public String getMarkdownOutputCharset() {
        return markdownOutputCharset;
    }

    public String getMarkdownOutputEOL() {
        return application.currentConverter.compileDynamicValues(markdownOutputEOL);
    }

    public String getMarkdownOutputEOF() {
        return application.currentConverter.compileDynamicValues(markdownOutputEOF);
    }

    public boolean isPdf() {
        return pdf;
    }

    public String getPdfSftp() {
        return pdfSftp;
    }

    public String getPdfSftpOutput() {
        return application.currentConverter.compileDynamicValues(pdfSftpOutput);
    }

    public Object getPdfJRXML() {
        return pdfJRXML;
    }

    public String getPdfOutput() {
        return application.currentConverter.compileDynamicValues(pdfOutput);
    }

    public boolean isPdfOutputAutoCreateDir() {
        return pdfOutputAutoCreateDir;
    }

    public boolean isTxt() {
        return txt;
    }

    public String getTxtSftp() {
        return txtSftp;
    }

    public String getTxtSftpOutput() {
        return application.currentConverter.compileDynamicValues(txtSftpOutput);
    }

    public String getTxtOutput() {
        return application.currentConverter.compileDynamicValues(txtOutput);
    }

    public boolean isTxtOutputAppend() {
        return txtOutputAppend;
    }

    public boolean isTxtOutputAutoCreateDir() {
        return txtOutputAutoCreateDir;
    }

    public String getTxtOutputCharset() {
        return txtOutputCharset;
    }

    public String getTxtOutputEOL() {
        return application.currentConverter.compileDynamicValues(txtOutputEOL);
    }

    public String getTxtOutputEOF() {
        return application.currentConverter.compileDynamicValues(txtOutputEOF);
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

    public String getCsvSftp() {
        return csvSftp;
    }

    public String getCsvSftpOutput() {
        return application.currentConverter.compileDynamicValues(csvSftpOutput);
    }

    public String getCsvOutput() {
        return application.currentConverter.compileDynamicValues(csvOutput);
    }

    public String getCsvSeparator() {
        return csvSeparator;
    }

    public boolean isCsvOutputAppend() {
        return csvOutputAppend;
    }

    public boolean isCsvOutputAutoCreateDir() {
        return csvOutputAutoCreateDir;
    }

    public String getCsvOutputCharset() {
        return csvOutputCharset;
    }

    public String getCsvOutputEOL() {
        return application.currentConverter.compileDynamicValues(csvOutputEOL);
    }

    public String getCsvOutputEOF() {
        return application.currentConverter.compileDynamicValues(csvOutputEOF);
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
        Converter currentConverter = application.currentConverter;
        List<String> values = new ArrayList<>();

        for (String sql : dbInsertPostSQL) {
            values.add(currentConverter.compileDynamicValues(sql));
        }

        return values;
    }

    public List<String> getDbInsertPreSQL() {
        Converter currentConverter = application.currentConverter;
        List<String> values = new ArrayList<>();

        for (String sql : dbInsertPreSQL) {
            values.add(currentConverter.compileDynamicValues(sql));
        }

        return values;
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
        Converter currentConverter = application.currentConverter;
        List<String> values = new ArrayList<>();

        for (String sql : dbUpdatePostSQL) {
            values.add(currentConverter.compileDynamicValues(sql));
        }

        return values;
    }

    public List<String> getDbUpdatePreSQL() {
        Converter currentConverter = application.currentConverter;
        List<String> values = new ArrayList<>();

        for (String sql : dbUpdatePreSQL) {
            values.add(currentConverter.compileDynamicValues(sql));
        }

        return values;
    }

    public List<OutputTypes> getOutputTypeList() {
        return outputTypeList;
    }

}