package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.LibraryMode;
import com.clevel.dconvers.output.OutputFactory;
import com.clevel.dconvers.output.OutputTypes;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

public class OutputConfig extends Config {

    private boolean src;
    private String srcSftp;
    private String srcSftpOutput;
    private String srcOutput;
    private boolean srcOutputAppend;
    private boolean srcOutputAutoCreateDir;
    private String srcOutputCharset;
    private String srcOutputEOL;
    private String srcOutputEOF;
    private String srcOwner;
    private String srcTable;
    private String srcId;
    private String srcDataSource;
    private String srcOutputs;


    private boolean tar;
    private boolean tarForSource;
    private boolean tarForName;
    private String tarSftp;
    private String tarSftpOutput;
    private String tarOutput;
    private boolean tarOutputAppend;
    private boolean tarOutputAutoCreateDir;
    private String tarOutputCharset;
    private String tarOutputEOL;
    private String tarOutputEOF;
    private String tarOutputs;


    private boolean sql;
    private String sqlSftp;
    private String sqlSftpOutput;
    private String sqlCombineOutput;
    private String sqlOutput;
    private boolean sqlOutputAppend;
    private boolean sqlOutputAutoCreateDir;
    private String sqlOutputCharset;
    private String sqlOutputEOL;
    private String sqlOutputEOF;
    private String sqlTable;
    private List<String> sqlColumn;
    private String sqlNameQuotes;
    private String sqlValueQuotes;
    private String sqlDBMS;
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
    private boolean markdownComment;
    private boolean markdownCommentDataSource;
    private boolean markdownCommentQuery;
    private boolean markdownTitle;
    private boolean markdownRowNumber;
    private boolean markdownMermaid;
    private boolean markdownMermaidFull;


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
    private String txtLengthMode;
    private List<String> txtFormat;
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
    private String csvOutputBOF;
    private String csvOutputEOL;
    private String csvOutputEOF;
    private boolean csvHeader;
    private List<String> csvHeaderColumn;
    private String csvSeparator;
    private String csvNullString;
    private List<String> csvFormat;
    private String csvFormatDate;
    private String csvFormatDatetime;
    private String csvFormatInteger;
    private String csvFormatDecimal;
    private String csvFormatString;


    private boolean dbInsert;
    private String dbInsertDataSource;
    private List<String> dbInsertColumnList;
    private String dbInsertTable;
    private String dbInsertNameQuotes;
    private String dbInsertValueQuotes;
    private List<String> dbInsertPostSQL;
    private List<String> dbInsertPreSQL;


    private boolean dbUpdate;
    private String dbUpdateDataSource;
    private List<String> dbUpdateColumnList;
    private String dbUpdateTable;
    private String dbUpdateId;
    private String dbUpdateNameQuotes;
    private String dbUpdateValueQuotes;
    private List<String> dbUpdatePostSQL;
    private List<String> dbUpdatePreSQL;


    private boolean dbExecute;
    private String dbExecuteDataSource;
    private String dbExecuteColumn;
    private String dbExecuteOutput;
    private List<String> dbExecutePostSQL;
    private List<String> dbExecutePreSQL;


    private boolean osVariable;
    private String osVariableName;
    private String osVariableValue;


    private boolean email;
    private String emailSftp;
    private String emailSftpOutput;
    private String emailOutput;
    private boolean emailOutputAppend;
    private boolean emailOutputAutoCreateDir;
    private String emailOutputCharset;
    private String emailOutputEOL;
    private String emailOutputEOF;
    private boolean emailComment;
    private boolean emailCommentDataSource;
    private boolean emailCommentQuery;
    private String emailSMTP;
    private String emailSubject;
    private String emailFrom;
    private String emailTo;
    private String emailCC;
    private String emailBCC;
    private boolean emailHtml;
    private String emailContent;


    private List<OutputTypes> outputTypeList;

    /**
     * Map<Output Plugin Name, <? extends OutputPluginConfig>>
     **/
    private HashMap<String, OutputPluginConfig> outputPluginConfigMap;

    public OutputConfig(DConvers dconvers, String baseProperty, Configuration baseProperties) {
        super(dconvers, baseProperty);
        this.properties = baseProperties;

        loadDefaults();
        if (LibraryMode.MANUAL != dconvers.switches.getLibraryMode()) {
            valid = loadProperties();
            if (valid) valid = validate();
        }

        log.trace("OutputConfig({}) is created", name);
    }

    @Override
    public void loadDefaults() {
        outputTypeList = new ArrayList<>();
        outputPluginConfigMap = new HashMap<>();

        // Default Properties for Source
        src = false;
        srcSftp = null;
        srcSftpOutput = null;
        srcOwner = "OWNER";                // name of owner to use as owner/schema name
        srcTable = "TABLE_NAME";           // name of table to use as table name
        srcId = "COLUMN_NAME";             // name of column to use as column name
        srcDataSource = "datasource-name";
        srcOutputs = "sql,md";
        srcOutput = "$[CAL:NAME(current)].conf";
        srcOutputAppend = false;
        srcOutputAutoCreateDir = true;
        srcOutputCharset = "UTF-8";
        srcOutputEOL = "\n";
        srcOutputEOF = "\n";

        // Default Properties for Target
        tar = false;
        tarForSource = false;
        tarForName = false;
        tarSftp = null;
        tarSftpOutput = null;
        tarOutputs = "sql,md";
        tarOutput = "$[CAL:NAME(current)].conf";
        tarOutputAppend = false;
        tarOutputAutoCreateDir = true;
        tarOutputCharset = "UTF-8";
        tarOutputEOL = "\n";
        tarOutputEOF = "\n";


        // Defaults Properties for SQL
        sql = false;
        sqlSftp = null;
        sqlSftpOutput = null;
        sqlCombineOutput = null;
        sqlOutput = "$[CAL:NAME(current)].sql";
        sqlOutputAppend = false;
        sqlOutputAutoCreateDir = true;
        sqlOutputCharset = "UTF-8";
        sqlOutputEOL = "\n";
        sqlOutputEOF = "\n";
        sqlTable = name;
        sqlColumn = new ArrayList<>();
        sqlNameQuotes = "";
        sqlValueQuotes = "\"";
        sqlDBMS = "MYSQL";
        sqlCreate = false;
        sqlInsert = false;
        sqlUpdate = false;
        sqlPreSQL = new ArrayList<>();
        sqlPostSQL = new ArrayList<>();

        // Default Properties for Markdown
        markdown = false;
        markdownSftp = null;
        markdownSftpOutput = null;
        markdownOutput = "$[CAL:NAME(current)].md";
        markdownOutputAppend = false;
        markdownOutputAutoCreateDir = true;
        markdownOutputCharset = "UTF-8";
        markdownOutputEOL = "\n";
        markdownOutputEOF = "\n";
        markdownComment = true;
        markdownCommentDataSource = true;
        markdownCommentQuery = true;
        markdownTitle = true;
        markdownRowNumber = true;
        markdownMermaid = true;
        markdownMermaidFull = true;

        // Default Properties for PDF
        pdf = false;
        pdfSftp = null;
        pdfSftpOutput = null;
        pdfOutput = "$[CAL:NAME(current)].pdf";
        pdfOutputAutoCreateDir = true;
        pdfJRXML = "";

        // TXT Output Properties
        txt = false;
        txtSftp = null;
        txtSftpOutput = null;
        txtOutput = "$[CAL:NAME(current)].txt";
        txtOutputAppend = false;
        txtOutputAutoCreateDir = true;
        txtOutputCharset = "UTF-8";
        txtOutputEOL = "\n";
        txtOutputEOF = "\n";
        txtLengthMode = "char"; // char or byte
        txtSeparator = "";
        txtFormat = new ArrayList<>();
        txtFormatDate = "yyyyMMdd";
        txtFormatDatetime = "yyyyMMddHHmmss";
        txtFillString = " ";
        txtFillNumber = "0";
        txtFillDate = " ";

        // CSV Output Properties
        csv = false;
        csvSftp = null;
        csvSftpOutput = null;
        csvOutput = "$[CAL:NAME(current)].csv";
        csvOutputAppend = false;
        csvOutputAutoCreateDir = true;
        csvOutputCharset = "UTF-8";
        csvOutputBOF = "";
        csvOutputEOL = "\n";
        csvOutputEOF = "\n";
        csvHeader = true;
        csvHeaderColumn = new ArrayList<>();
        csvSeparator = ",";
        csvNullString = "";
        csvFormat = new ArrayList<>();
        csvFormatDate = "dd/MM/yyyy";
        csvFormatDatetime = "dd/MM/yyyy HH:mm:ss";
        csvFormatInteger = "###0";
        csvFormatDecimal = "###0.####";
        csvFormatString = "";

        // DBInsert Output Properties
        dbInsert = false;
        dbInsertDataSource = "";
        dbInsertColumnList = new ArrayList<>();
        dbInsertTable = name;
        dbInsertNameQuotes = "";
        dbInsertValueQuotes = "\"";
        dbInsertPreSQL = new ArrayList<>();
        dbInsertPostSQL = new ArrayList<>();

        // DBUpdate Output Properties
        dbUpdate = false;
        dbUpdateDataSource = "";
        dbUpdateColumnList = new ArrayList<>();
        dbUpdateTable = name;
        dbUpdateNameQuotes = "";
        dbUpdateValueQuotes = "\"";
        dbUpdateId = "id";
        dbUpdatePreSQL = new ArrayList<>();
        dbUpdatePostSQL = new ArrayList<>();

        // DBExecute Output Properties
        dbExecute = false;
        dbExecuteDataSource = "";
        dbExecuteColumn = "sql";
        dbExecuteOutput = "dbexecute_history_$[CAL:NAME(current)].log";
        dbExecutePreSQL = new ArrayList<>();
        dbExecutePostSQL = new ArrayList<>();

        // OS Variable Output Properties
        osVariable = false;
        osVariableName = "VARIABLE";
        osVariableValue = "VALUE";

        // Default Properties for Email
        email = false;
        emailSftp = null;
        emailSftpOutput = null;
        emailOutput = "$[CAL:NAME(current)].md";
        emailOutputAppend = false;
        emailOutputAutoCreateDir = true;
        emailOutputCharset = "UTF-8";
        emailOutputEOL = "\n";
        emailOutputEOF = "\n";
        emailComment = true;
        emailCommentDataSource = true;
        emailCommentQuery = true;
        emailSMTP = null;
        emailSubject = null;
        emailFrom = null;
        emailTo = null;
        emailCC = null;
        emailBCC = null;
        emailHtml = false;
        emailContent = null;


        /*TODO: loadDefaults*/
    }

    @Override
    protected boolean loadProperties() {
        String baseProperty = name;

        String key = Property.SRC.prefixKey(baseProperty);
        src = properties.getBoolean(key, src);
        if (src) {
            outputTypeList.add(OutputTypes.CONVERTER_SOURCE_FILE);

            Configuration srcProperties = properties.subset(key);
            srcOwner = getPropertyString(srcProperties, Property.OWNER.key(), srcOwner);
            srcTable = getPropertyString(srcProperties, Property.TABLE.key(), srcTable);
            srcId = getPropertyString(srcProperties, Property.ID.key(), srcId);
            srcDataSource = getPropertyString(srcProperties, Property.DATA_SOURCE.key(), srcDataSource);
            srcOutputs = getPropertyString(srcProperties, Property.OUTPUT_TYPES.key(), srcOutputs);
            srcSftp = getPropertyString(srcProperties, Property.SFTP.key(), srcSftp);
            srcSftpOutput = getPropertyString(srcProperties, Property.SFTP.connectKey(Property.OUTPUT_FILE), srcSftpOutput);
            srcOutput = getPropertyString(srcProperties, Property.OUTPUT_FILE.key(), srcOutput);
            srcOutputAppend = srcProperties.getBoolean(Property.OUTPUT_APPEND.key(), srcOutputAppend);
            srcOutputAutoCreateDir = srcProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), srcOutputAutoCreateDir);
            srcOutputCharset = getPropertyString(srcProperties, Property.OUTPUT_CHARSET.key(), srcOutputCharset);
            srcOutputEOL = getPropertyString(srcProperties, Property.OUTPUT_EOL.key(), srcOutputEOL);
            srcOutputEOF = getPropertyString(srcProperties, Property.OUTPUT_EOF.key(), srcOutputEOF);
        }

        key = Property.TAR.prefixKey(baseProperty);
        tar = properties.getBoolean(key, tar);
        if (tar) {
            outputTypeList.add(OutputTypes.CONVERTER_TARGET_FILE);

            Configuration tarProperties = properties.subset(key);
            tarOutputs = getPropertyString(tarProperties, Property.OUTPUT_TYPES.key(), tarOutputs);
            tarForSource = tarProperties.getBoolean(Property.FOR.connectKey(Property.SOURCE), tarForSource);
            tarForName = tarProperties.getBoolean(Property.FOR.connectKey(Property.NAME), tarForName);
            tarSftp = getPropertyString(tarProperties, Property.SFTP.key(), tarSftp);
            tarSftpOutput = getPropertyString(tarProperties, Property.SFTP.connectKey(Property.OUTPUT_FILE), tarSftpOutput);
            tarOutput = getPropertyString(tarProperties, Property.OUTPUT_FILE.key(), tarOutput);
            tarOutputAppend = tarProperties.getBoolean(Property.OUTPUT_APPEND.key(), tarOutputAppend);
            tarOutputAutoCreateDir = tarProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), tarOutputAutoCreateDir);
            tarOutputCharset = getPropertyString(tarProperties, Property.OUTPUT_CHARSET.key(), tarOutputCharset);
            tarOutputEOL = getPropertyString(tarProperties, Property.OUTPUT_EOL.key(), tarOutputEOL);
            tarOutputEOF = getPropertyString(tarProperties, Property.OUTPUT_EOF.key(), tarOutputEOF);
        }

        key = Property.SQL.prefixKey(baseProperty);
        sql = properties.getBoolean(key, sql);
        if (sql) {
            outputTypeList.add(OutputTypes.SQL_FILE);

            Configuration sqlProperties = properties.subset(key);
            sqlSftp = getPropertyString(sqlProperties, Property.SFTP.key(), sqlSftp);
            sqlSftpOutput = getPropertyString(sqlProperties, Property.SFTP.connectKey(Property.OUTPUT_FILE), sqlSftpOutput);
            sqlCombineOutput = getPropertyString(sqlProperties, Property.COMBINE.connectKey(Property.OUTPUT_FILE), sqlCombineOutput);
            sqlOutput = getPropertyString(sqlProperties, Property.OUTPUT_FILE.key(), sqlOutput);
            sqlOutputAppend = sqlProperties.getBoolean(Property.OUTPUT_APPEND.key(), sqlOutputAppend);
            sqlOutputAutoCreateDir = sqlProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), sqlOutputAutoCreateDir);
            sqlOutputCharset = getPropertyString(sqlProperties, Property.OUTPUT_CHARSET.key(), sqlOutputCharset);
            sqlOutputEOL = getPropertyString(sqlProperties, Property.OUTPUT_EOL.key(), sqlOutputEOL);
            sqlOutputEOF = getPropertyString(sqlProperties, Property.OUTPUT_EOF.key(), sqlOutputEOF);
            sqlTable = getPropertyString(sqlProperties, Property.TABLE.key(), sqlTable);
            sqlDBMS = getPropertyString(sqlProperties, Property.DBMS.key(), sqlDBMS);
            sqlColumn = getStringList(sqlProperties, Property.COLUMN.key());
            sqlNameQuotes = getPropertyString(sqlProperties, Property.QUOTES.connectKey(Property.NAME), sqlNameQuotes);
            sqlValueQuotes = getPropertyString(sqlProperties, Property.QUOTES.connectKey(Property.VALUE), sqlValueQuotes);
            sqlCreate = sqlProperties.getBoolean(Property.CREATE.key(), sqlCreate);
            sqlInsert = sqlProperties.getBoolean(Property.INSERT.key(), sqlInsert);
            sqlUpdate = sqlProperties.getBoolean(Property.UPDATE.key(), sqlUpdate);
            sqlPreSQL = getSQLStringList(sqlProperties, Property.PRE_SQL.key());
            sqlPostSQL = getSQLStringList(sqlProperties, Property.POST_SQL.key());
        }

        key = Property.MARKDOWN.prefixKey(baseProperty);
        markdown = properties.getBoolean(key, markdown);
        if (markdown) {
            outputTypeList.add(OutputTypes.MARKDOWN_FILE);

            Configuration markdownProperties = properties.subset(key);
            markdownSftp = getPropertyString(markdownProperties, Property.SFTP.key(), markdownSftp);
            markdownSftpOutput = getPropertyString(markdownProperties, Property.SFTP.connectKey(Property.OUTPUT_FILE), markdownSftpOutput);
            markdownOutput = getPropertyString(markdownProperties, Property.OUTPUT_FILE.key(), markdownOutput);
            markdownOutputAppend = markdownProperties.getBoolean(Property.OUTPUT_APPEND.key(), markdownOutputAppend);
            markdownOutputAutoCreateDir = markdownProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), markdownOutputAutoCreateDir);
            markdownOutputCharset = getPropertyString(markdownProperties, Property.OUTPUT_CHARSET.key(), markdownOutputCharset);
            markdownOutputEOL = getPropertyString(markdownProperties, Property.OUTPUT_EOL.key(), markdownOutputEOL);
            markdownOutputEOF = getPropertyString(markdownProperties, Property.OUTPUT_EOF.key(), markdownOutputEOF);

            markdownComment = markdownProperties.getBoolean(Property.COMMENT.key(), markdownComment);
            markdownCommentDataSource = markdownProperties.getBoolean(Property.COMMENT.connectKey(Property.DATA_SOURCE), markdownCommentDataSource);
            markdownCommentQuery = markdownProperties.getBoolean(Property.COMMENT.connectKey(Property.QUERY), markdownCommentQuery);
            markdownTitle = markdownProperties.getBoolean(Property.TITLE.key(), markdownTitle);
            markdownRowNumber = markdownProperties.getBoolean(Property.ROW_NUMBER.key(), markdownRowNumber);
            markdownMermaid = markdownProperties.getBoolean(Property.MERMAID.key(), markdownMermaid);
            markdownMermaidFull = markdownProperties.getBoolean(Property.MERMAID.connectKey(Property.FULL.key()), markdownMermaidFull);
        }

        key = Property.PDF_TABLE.prefixKey(baseProperty);
        pdf = properties.getBoolean(key, pdf);
        if (pdf) {
            outputTypeList.add(OutputTypes.PDF_FILE);

            Configuration pdfProperties = properties.subset(key);
            pdfSftp = getPropertyString(pdfProperties, Property.SFTP.key(), pdfSftp);
            pdfSftpOutput = getPropertyString(pdfProperties, Property.SFTP.connectKey(Property.OUTPUT_FILE), pdfSftpOutput);
            pdfOutput = getPropertyString(pdfProperties, Property.OUTPUT_FILE.key(), pdfOutput);
            pdfOutputAutoCreateDir = pdfProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), pdfOutputAutoCreateDir);
            String jrxml = getPropertyString(pdfProperties, Property.OUTPUT_FILE.key(), (String) pdfJRXML);
            if (jrxml.isEmpty()) {
                pdfJRXML = getDefaultJRXML();
            } else {
                pdfJRXML = jrxml;
            }
        }

        key = Property.TXT.prefixKey(baseProperty);
        txt = properties.getBoolean(key, txt);
        if (txt) {
            outputTypeList.add(OutputTypes.TXT_FILE);

            Configuration txtProperties = properties.subset(key);
            txtSftp = getPropertyString(txtProperties, Property.SFTP.key(), txtSftp);
            txtSftpOutput = getPropertyString(txtProperties, Property.SFTP.connectKey(Property.OUTPUT_FILE), txtSftpOutput);
            txtOutput = getPropertyString(txtProperties, Property.OUTPUT_FILE.key(), txtOutput);
            txtOutputAppend = txtProperties.getBoolean(Property.OUTPUT_APPEND.key(), txtOutputAppend);
            txtOutputAutoCreateDir = txtProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), txtOutputAutoCreateDir);
            txtOutputCharset = getPropertyString(txtProperties, Property.OUTPUT_CHARSET.key(), txtOutputCharset);
            txtOutputEOL = getPropertyString(txtProperties, Property.OUTPUT_EOL.key(), txtOutputEOL);
            txtOutputEOF = getPropertyString(txtProperties, Property.OUTPUT_EOF.key(), txtOutputEOF);
            txtLengthMode = getPropertyString(txtProperties, Property.LENGTH_MODE.key(), txtLengthMode);
            txtSeparator = getPropertyString(txtProperties, Property.SEPARATOR.key(), txtSeparator);
            txtFormat = getStringList(txtProperties, Property.FORMAT.key());
            txtFormatDate = getPropertyString(txtProperties, Property.FORMAT_DATE.key(), txtFormatDate);
            txtFormatDatetime = getPropertyString(txtProperties, Property.FORMAT_DATETIME.key(), txtFormatDatetime);
            txtFillString = getPropertyString(txtProperties, Property.FILL_STRING.key(), txtFillString);
            txtFillNumber = getPropertyString(txtProperties, Property.FILL_NUMBER.key(), txtFillNumber);
            txtFillDate = getPropertyString(txtProperties, Property.FILL_DATE.key(), txtFillDate);
        }

        key = Property.CSV.prefixKey(baseProperty);
        csv = properties.getBoolean(key, csv);
        if (csv) {
            outputTypeList.add(OutputTypes.CSV_FILE);

            Configuration csvProperties = properties.subset(key);
            csvSftp = getPropertyString(csvProperties, Property.SFTP.key(), csvSftp);
            csvSftpOutput = getPropertyString(csvProperties, Property.SFTP.connectKey(Property.OUTPUT_FILE), csvSftpOutput);
            csvOutput = getPropertyString(csvProperties, Property.OUTPUT_FILE.key(), csvOutput);
            csvOutputAppend = csvProperties.getBoolean(Property.OUTPUT_APPEND.key(), csvOutputAppend);
            csvOutputAutoCreateDir = csvProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), csvOutputAutoCreateDir);
            csvOutputCharset = getPropertyString(csvProperties, Property.OUTPUT_CHARSET.key(), csvOutputCharset);
            csvOutputBOF = getPropertyString(csvProperties, Property.OUTPUT_BOF.key(), csvOutputBOF);
            csvOutputEOL = getPropertyString(csvProperties, Property.OUTPUT_EOL.key(), csvOutputEOL);
            csvOutputEOF = getPropertyString(csvProperties, Property.OUTPUT_EOF.key(), csvOutputEOF);
            csvSeparator = getPropertyString(csvProperties, Property.SEPARATOR.key(), csvSeparator);
            csvNullString = getPropertyString(csvProperties, Property.NULL.key(), csvNullString);
            csvHeader = csvProperties.getBoolean(Property.HEADER.key(), csvHeader);
            csvHeaderColumn = getStringList(csvProperties, Property.HEADER.connectKey(Property.COLUMN));
            csvFormat = getStringList(csvProperties, Property.FORMAT.key());
            csvFormatDate = getPropertyString(csvProperties, Property.FORMAT_DATE.key(), csvFormatDate);
            csvFormatDatetime = getPropertyString(csvProperties, Property.FORMAT_DATETIME.key(), csvFormatDatetime);
            csvFormatInteger = getPropertyString(csvProperties, Property.FORMAT_INTEGER.key(), csvFormatInteger);
            csvFormatDecimal = getPropertyString(csvProperties, Property.FORMAT_DECIMAL.key(), csvFormatDecimal);
            csvFormatString = getPropertyString(csvProperties, Property.FORMAT_STRING.key(), csvFormatString);
        }

        key = Property.DBINSERT.prefixKey(baseProperty);
        dbInsert = properties.getBoolean(key, dbInsert);
        if (dbInsert) {
            outputTypeList.add(OutputTypes.INSERT_DB);

            Configuration dbInsertProperties = properties.subset(key);
            dbInsertDataSource = getPropertyString(dbInsertProperties, Property.DATA_SOURCE.key(), dbInsertDataSource);
            dbInsertColumnList = getStringList(dbInsertProperties, Property.COLUMN.key());
            dbInsertTable = getPropertyString(dbInsertProperties, Property.TABLE.key(), dbInsertTable);
            dbInsertNameQuotes = getPropertyString(dbInsertProperties, Property.QUOTES.connectKey(Property.NAME), dbInsertNameQuotes);
            dbInsertValueQuotes = getPropertyString(dbInsertProperties, Property.QUOTES.connectKey(Property.VALUE), dbInsertValueQuotes);
            dbInsertPreSQL = getSQLStringList(dbInsertProperties, Property.PRE_SQL.key());
            dbInsertPostSQL = getSQLStringList(dbInsertProperties, Property.POST_SQL.key());
        }

        key = Property.DBUPDATE.prefixKey(baseProperty);
        dbUpdate = properties.getBoolean(key, dbUpdate);
        if (dbUpdate) {
            outputTypeList.add(OutputTypes.UPDATE_DB);

            Configuration dbUpdateProperties = properties.subset(key);
            dbUpdateDataSource = getPropertyString(dbUpdateProperties, Property.DATA_SOURCE.key(), dbUpdateDataSource);
            dbUpdateColumnList = getStringList(dbUpdateProperties, Property.COLUMN.key());
            dbUpdateTable = getPropertyString(dbUpdateProperties, Property.TABLE.key(), dbUpdateTable);
            dbUpdateId = getPropertyString(properties, Property.ID.key(), dbUpdateId);
            dbUpdateNameQuotes = dbUpdateProperties.getString(Property.QUOTES.connectKey(Property.NAME), dbUpdateNameQuotes);
            dbUpdateValueQuotes = dbUpdateProperties.getString(Property.QUOTES.connectKey(Property.VALUE), dbUpdateValueQuotes);
            dbUpdatePreSQL = getSQLStringList(dbUpdateProperties, Property.PRE_SQL.key());
            dbUpdatePostSQL = getSQLStringList(dbUpdateProperties, Property.POST_SQL.key());
        }

        key = Property.DBEXECUTE.prefixKey(baseProperty);
        dbExecute = properties.getBoolean(key, dbExecute);
        if (dbExecute) {
            outputTypeList.add(OutputTypes.EXECUTE_DB);

            Configuration dbExecuteProperties = properties.subset(key);
            dbExecuteDataSource = getPropertyString(dbExecuteProperties, Property.DATA_SOURCE.key(), dbExecuteDataSource);
            dbExecuteColumn = getPropertyString(dbExecuteProperties, Property.COLUMN.key(), dbExecuteColumn);
            dbExecuteOutput = getPropertyString(dbExecuteProperties, Property.OUTPUT_FILE.key(), dbExecuteOutput);
            dbExecutePreSQL = getSQLStringList(dbExecuteProperties, Property.PRE_SQL.key());
            dbExecutePostSQL = getSQLStringList(dbExecuteProperties, Property.POST_SQL.key());
        }

        key = Property.OSVARIABLE.prefixKey(baseProperty);
        osVariable = properties.getBoolean(key, osVariable);
        if (osVariable) {
            outputTypeList.add(OutputTypes.OS_VARIABLE);

            Configuration osVariableProperties = properties.subset(key);
            osVariableName = getPropertyString(osVariableProperties, Property.NAME.key(), osVariableName);
            osVariableValue = getPropertyString(osVariableProperties, Property.VALUE.key(), osVariableValue);
        }

        key = Property.EMAIL.prefixKey(baseProperty);
        email = properties.getBoolean(key, email);
        if (email) {
            outputTypeList.add(OutputTypes.EMAIL);

            Configuration emailProperties = properties.subset(key);
            emailSftp = getPropertyString(emailProperties, Property.SFTP.key(), emailSftp);
            emailSftpOutput = getPropertyString(emailProperties, Property.SFTP.connectKey(Property.OUTPUT_FILE), emailSftpOutput);
            emailOutput = getPropertyString(emailProperties, Property.OUTPUT_FILE.key(), emailOutput);
            emailOutputAppend = emailProperties.getBoolean(Property.OUTPUT_APPEND.key(), emailOutputAppend);
            emailOutputAutoCreateDir = emailProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), emailOutputAutoCreateDir);
            emailOutputCharset = getPropertyString(emailProperties, Property.OUTPUT_CHARSET.key(), emailOutputCharset);
            emailOutputEOL = getPropertyString(emailProperties, Property.OUTPUT_EOL.key(), emailOutputEOL);
            emailOutputEOF = getPropertyString(emailProperties, Property.OUTPUT_EOF.key(), emailOutputEOF);

            emailComment = emailProperties.getBoolean(Property.COMMENT.key(), emailComment);
            emailCommentDataSource = emailProperties.getBoolean(Property.COMMENT.connectKey(Property.DATA_SOURCE), emailCommentDataSource);
            emailCommentQuery = emailProperties.getBoolean(Property.COMMENT.connectKey(Property.QUERY), emailCommentQuery);

            emailSMTP = getPropertyString(emailProperties, Property.SMTP.key(), emailSMTP);
            emailSubject = getPropertyString(emailProperties, Property.SUBJECT.key(), emailSubject);
            emailFrom = getPropertyString(emailProperties, Property.FROM.key(), emailFrom);
            emailTo = getPropertyString(emailProperties, Property.TO.key(), emailTo);
            emailCC = getPropertyString(emailProperties, Property.CC.key(), emailCC);
            emailBCC = getPropertyString(emailProperties, Property.BCC.key(), emailBCC);
            emailHtml = emailProperties.getBoolean(Property.HTML.key(), emailHtml);
            emailContent = getPropertyString(emailProperties, Property.CONTENT.key(), emailContent);
        }

        // Load Plugin Config into outputPluginConfigMap
        HashMap<String, Class> plugins = OutputTypes.getPlugins();
        //log.debug("OutputConfig.loadProperties found {} output-plugins", plugins.size());
        for (String pluginName : plugins.keySet()) {
            key = baseProperty + "." + pluginName;
            boolean enabled = properties.getBoolean(key, false);
            //log.debug("OutputConfig.loadProperties.plugin={}, baseProperty={}, enabled={}", pluginName, baseProperty, enabled);
            if (enabled) {
                OutputTypes type = OutputTypes.parse(pluginName);
                outputTypeList.add(type);

                OutputPluginConfig pluginConfig = OutputFactory.getPluginConfig(dconvers, type);
                pluginConfig.loadConfig(properties.subset(key));

                //log.debug("OutputConfig.loadProperties.plugin({})={}", pluginName, pluginConfig.toString());
                outputPluginConfigMap.put(type.getName(), pluginConfig);
            }
        }
        //log.debug("OutputTypeList(name:{})={}", name, outputTypeList.toString());

        return true;
    }

    public HashMap<String, OutputPluginConfig> getOutputPluginConfigMap() {
        return outputPluginConfigMap;
    }

    private List<String> getStringList(Configuration properties, String key) {
        List<Object> objectList;
        try {
            objectList = properties.getList(key);
        } catch (ConversionException ex) {
            objectList = new ArrayList<>();
        }

        String value;
        List<String> stringList = new ArrayList<>();
        for (Object obj : objectList) {
            value = obj.toString();
            if (value.contains(",")) {
                stringList.addAll(Arrays.asList(value.split("[,]")));
            } else {
                stringList.add(value);
            }
        }

        return stringList;
    }

    private List<String> getSQLStringList(Configuration properties, String key) {
        List<Object> objectList;
        try {
            objectList = properties.getList(key);
        } catch (ConversionException ex) {
            objectList = new ArrayList<>();
        }

        String value;
        List<String> stringList = new ArrayList<>();
        for (Object obj : objectList) {
            value = obj.toString();
            stringList.add(value);
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

    public boolean isSrc() {
        return src;
    }

    public String getSrcSftp() {
        return srcSftp;
    }

    public String getSrcSftpOutput() {
        return srcSftpOutput;
    }

    public String getSrcOutput() {
        return srcOutput;
    }

    public boolean isSrcOutputAppend() {
        return srcOutputAppend;
    }

    public boolean isSrcOutputAutoCreateDir() {
        return srcOutputAutoCreateDir;
    }

    public String getSrcOutputCharset() {
        return srcOutputCharset;
    }

    public String getSrcOutputEOL() {
        return srcOutputEOL;
    }

    public String getSrcOutputEOF() {
        return srcOutputEOF;
    }

    public String getSrcOwner() {
        return srcOwner;
    }

    public String getSrcTable() {
        return srcTable;
    }

    public String getSrcId() {
        return srcId;
    }

    public String getSrcDataSource() {
        return srcDataSource;
    }

    public String getSrcOutputs() {
        return srcOutputs;
    }

    public boolean isTar() {
        return tar;
    }

    public boolean isTarForSource() {
        return tarForSource;
    }

    public boolean isTarForName() {
        return tarForName;
    }

    public String getTarSftp() {
        return tarSftp;
    }

    public String getTarSftpOutput() {
        return tarSftpOutput;
    }

    public String getTarOutput() {
        return tarOutput;
    }

    public boolean isTarOutputAppend() {
        return tarOutputAppend;
    }

    public boolean isTarOutputAutoCreateDir() {
        return tarOutputAutoCreateDir;
    }

    public String getTarOutputCharset() {
        return tarOutputCharset;
    }

    public String getTarOutputEOL() {
        return tarOutputEOL;
    }

    public String getTarOutputEOF() {
        return tarOutputEOF;
    }

    public String getTarOutputs() {
        return tarOutputs;
    }

    public boolean isSql() {
        return sql;
    }

    public String getSqlSftp() {
        return sqlSftp;
    }

    public String getSqlSftpOutput() {
        return dconvers.currentConverter.compileDynamicValues(sqlSftpOutput);
    }

    public String getSqlOutput() {
        return dconvers.currentConverter.compileDynamicValues(sqlOutput);
    }

    public String getSqlCombineOutput() {
        return dconvers.currentConverter.compileDynamicValues(sqlCombineOutput);
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
        return dconvers.currentConverter.compileDynamicValues(sqlOutputEOL);
    }

    public String getSqlOutputEOF() {
        return dconvers.currentConverter.compileDynamicValues(sqlOutputEOF);
    }

    public String getSqlTable() {
        return sqlTable;
    }

    public List<String> getSqlColumn() {
        return sqlColumn;
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

    public String getSqlDBMS() {
        return sqlDBMS;
    }

    public List<String> getSqlPostSQL() {
        return dconvers.currentConverter.compileDynamicValues(sqlPostSQL);
    }

    public List<String> getSqlPreSQL() {
        return dconvers.currentConverter.compileDynamicValues(sqlPreSQL);
    }

    public boolean isMarkdown() {
        return markdown;
    }

    public String getMarkdownSftp() {
        return markdownSftp;
    }

    public String getMarkdownSftpOutput() {
        return dconvers.currentConverter.compileDynamicValues(markdownSftpOutput);
    }

    public String getMarkdownOutput() {
        return dconvers.currentConverter.compileDynamicValues(markdownOutput);
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
        return dconvers.currentConverter.compileDynamicValues(markdownOutputEOL);
    }

    public String getMarkdownOutputEOF() {
        return dconvers.currentConverter.compileDynamicValues(markdownOutputEOF);
    }

    public boolean isMarkdownComment() {
        return markdownComment;
    }

    public boolean isMarkdownCommentDataSource() {
        return markdownCommentDataSource;
    }

    public boolean isMarkdownCommentQuery() {
        return markdownCommentQuery;
    }

    public boolean isMarkdownTitle() {
        return markdownTitle;
    }

    public boolean isMarkdownRowNumber() {
        return markdownRowNumber;
    }

    public boolean isMarkdownMermaid() {
        return markdownMermaid;
    }

    public boolean isMarkdownMermaidFull() {
        return markdownMermaidFull;
    }

    public boolean isPdf() {
        return pdf;
    }

    public String getPdfSftp() {
        return pdfSftp;
    }

    public String getPdfSftpOutput() {
        return dconvers.currentConverter.compileDynamicValues(pdfSftpOutput);
    }

    public Object getPdfJRXML() {
        return pdfJRXML;
    }

    public String getPdfOutput() {
        return dconvers.currentConverter.compileDynamicValues(pdfOutput);
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
        return dconvers.currentConverter.compileDynamicValues(txtSftpOutput);
    }

    public String getTxtOutput() {
        return dconvers.currentConverter.compileDynamicValues(txtOutput);
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
        return dconvers.currentConverter.compileDynamicValues(txtOutputEOL);
    }

    public String getTxtOutputEOF() {
        return dconvers.currentConverter.compileDynamicValues(txtOutputEOF);
    }

    public String getTxtLengthMode() {
        return txtLengthMode;
    }

    public String getTxtSeparator() {
        return txtSeparator;
    }

    public List<String> getTxtFormat() {
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
        return dconvers.currentConverter.compileDynamicValues(csvSftpOutput);
    }

    public String getCsvOutput() {
        return dconvers.currentConverter.compileDynamicValues(csvOutput);
    }

    public String getCsvSeparator() {
        return csvSeparator;
    }

    public String getCsvNullString() {
        return csvNullString;
    }

    public List<String> getCsvHeaderColumn() {
        return csvHeaderColumn;
    }

    public boolean isCsvHeader() {
        return csvHeader;
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

    public String getCsvOutputBOF() {
        return dconvers.currentConverter.compileDynamicValues(csvOutputBOF);
    }

    public String getCsvOutputEOL() {
        return dconvers.currentConverter.compileDynamicValues(csvOutputEOL);
    }

    public String getCsvOutputEOF() {
        return dconvers.currentConverter.compileDynamicValues(csvOutputEOF);
    }

    public List<String> getCsvFormat() {
        return csvFormat;
    }

    public String getCsvFormatDate() {
        return csvFormatDate;
    }

    public String getCsvFormatDatetime() {
        return csvFormatDatetime;
    }

    public String getCsvFormatInteger() {
        return csvFormatInteger;
    }

    public String getCsvFormatDecimal() {
        return csvFormatDecimal;
    }

    public String getCsvFormatString() {
        return csvFormatString;
    }

    public boolean isDbInsert() {
        return dbInsert;
    }

    public String getDbInsertDataSource() {
        return dbInsertDataSource;
    }

    public List<String> getDbInsertColumnList() {
        return dbInsertColumnList;
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
        return dconvers.currentConverter.compileDynamicValues(dbInsertPostSQL);
    }

    public List<String> getDbInsertPreSQL() {
        return dconvers.currentConverter.compileDynamicValues(dbInsertPreSQL);
    }

    public boolean isDbUpdate() {
        return dbUpdate;
    }

    public String getDbUpdateDataSource() {
        return dbUpdateDataSource;
    }

    public List<String> getDbUpdateColumnList() {
        return dbUpdateColumnList;
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
        return dconvers.currentConverter.compileDynamicValues(dbUpdatePostSQL);
    }

    public List<String> getDbUpdatePreSQL() {
        return dconvers.currentConverter.compileDynamicValues(dbUpdatePreSQL);
    }

    public boolean isDbExecute() {
        return dbExecute;
    }

    public String getDbExecuteDataSource() {
        return dbExecuteDataSource;
    }

    public String getDbExecuteColumn() {
        return dbExecuteColumn;
    }

    public String getDbExecuteOutput() {
        return dconvers.currentConverter.compileDynamicValues(dbExecuteOutput);
    }

    public List<String> getDbExecutePostSQL() {
        return dconvers.currentConverter.compileDynamicValues(dbExecutePostSQL);
    }

    public List<String> getDbExecutePreSQL() {
        return dconvers.currentConverter.compileDynamicValues(dbExecutePreSQL);
    }

    public List<OutputTypes> getOutputTypeList() {
        return outputTypeList;
    }

    public boolean isOsVariable() {
        return osVariable;
    }

    public String getOsVariableName() {
        return osVariableName;
    }

    public String getOsVariableValue() {
        return osVariableValue;
    }

    public boolean isEmail() {
        return email;
    }

    public String getEmailSftp() {
        return emailSftp;
    }

    public String getEmailSftpOutput() {
        return dconvers.currentConverter.compileDynamicValues(emailSftpOutput);
    }

    public String getEmailOutput() {
        return dconvers.currentConverter.compileDynamicValues(emailOutput);
    }

    public boolean isEmailOutputAppend() {
        return emailOutputAppend;
    }

    public boolean isEmailOutputAutoCreateDir() {
        return emailOutputAutoCreateDir;
    }

    public String getEmailOutputCharset() {
        return emailOutputCharset;
    }

    public String getEmailOutputEOL() {
        return emailOutputEOL;
    }

    public String getEmailOutputEOF() {
        return emailOutputEOF;
    }

    public boolean isEmailComment() {
        return emailComment;
    }

    public boolean isEmailCommentDataSource() {
        return emailCommentDataSource;
    }

    public boolean isEmailCommentQuery() {
        return emailCommentQuery;
    }

    public String getEmailSMTP() {
        return emailSMTP;
    }

    public String getEmailSubject() {
        return dconvers.currentConverter.compileDynamicValues(emailSubject);
    }

    public String getEmailFrom() {
        return dconvers.currentConverter.compileDynamicValues(emailFrom);
    }

    public String getEmailTo() {
        return dconvers.currentConverter.compileDynamicValues(emailTo);
    }

    public String getEmailCC() {
        return dconvers.currentConverter.compileDynamicValues(emailCC);
    }

    public String getEmailBCC() {
        return dconvers.currentConverter.compileDynamicValues(emailBCC);
    }

    public boolean isEmailHtml() {
        return emailHtml;
    }

    public String getEmailContent() {
        return dconvers.currentConverter.compileDynamicValues(emailContent);
    }

    public boolean needOutput() {
        return outputTypeList.size() > 0;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("src", src)
                .append("srcSftp", srcSftp)
                .append("srcSftpOutput", srcSftpOutput)
                .append("srcOutput", srcOutput)
                .append("srcOutputAppend", srcOutputAppend)
                .append("srcOutputAutoCreateDir", srcOutputAutoCreateDir)
                .append("srcOutputCharset", srcOutputCharset)
                .append("srcOutputEOL", srcOutputEOL)
                .append("srcOutputEOF", srcOutputEOF)
                .append("srcOwner", srcOwner)
                .append("srcTable", srcTable)
                .append("srcId", srcId)
                .append("srcDataSource", srcDataSource)
                .append("srcOutputs", srcOutputs)
                .append("tar", tar)
                .append("tarForSource", tarForSource)
                .append("tarForName", tarForName)
                .append("tarSftp", tarSftp)
                .append("tarSftpOutput", tarSftpOutput)
                .append("tarOutput", tarOutput)
                .append("tarOutputAppend", tarOutputAppend)
                .append("tarOutputAutoCreateDir", tarOutputAutoCreateDir)
                .append("tarOutputCharset", tarOutputCharset)
                .append("tarOutputEOL", tarOutputEOL)
                .append("tarOutputEOF", tarOutputEOF)
                .append("tarOutputs", tarOutputs)
                .append("sql", sql)
                .append("sqlSftp", sqlSftp)
                .append("sqlSftpOutput", sqlSftpOutput)
                .append("sqlCombineOutput", sqlCombineOutput)
                .append("sqlOutput", sqlOutput)
                .append("sqlOutputAppend", sqlOutputAppend)
                .append("sqlOutputAutoCreateDir", sqlOutputAutoCreateDir)
                .append("sqlOutputCharset", sqlOutputCharset)
                .append("sqlOutputEOL", sqlOutputEOL)
                .append("sqlOutputEOF", sqlOutputEOF)
                .append("sqlTable", sqlTable)
                .append("sqlColumn", sqlColumn)
                .append("sqlNameQuotes", sqlNameQuotes)
                .append("sqlValueQuotes", sqlValueQuotes)
                .append("sqlDBMS", sqlDBMS)
                .append("sqlCreate", sqlCreate)
                .append("sqlInsert", sqlInsert)
                .append("sqlUpdate", sqlUpdate)
                .append("sqlPostSQL", sqlPostSQL)
                .append("sqlPreSQL", sqlPreSQL)
                .append("markdown", markdown)
                .append("markdownSftp", markdownSftp)
                .append("markdownSftpOutput", markdownSftpOutput)
                .append("markdownOutput", markdownOutput)
                .append("markdownOutputAppend", markdownOutputAppend)
                .append("markdownOutputAutoCreateDir", markdownOutputAutoCreateDir)
                .append("markdownOutputCharset", markdownOutputCharset)
                .append("markdownOutputEOL", markdownOutputEOL)
                .append("markdownOutputEOF", markdownOutputEOF)
                .append("markdownComment", markdownComment)
                .append("markdownCommentDataSource", markdownCommentDataSource)
                .append("markdownCommentQuery", markdownCommentQuery)
                .append("markdownTitle", markdownTitle)
                .append("markdownMermaid", markdownMermaid)
                .append("markdownMermaidFull", markdownMermaidFull)
                .append("pdf", pdf)
                .append("pdfJRXML", pdfJRXML)
                .append("pdfSftp", pdfSftp)
                .append("pdfSftpOutput", pdfSftpOutput)
                .append("pdfOutput", pdfOutput)
                .append("pdfOutputAutoCreateDir", pdfOutputAutoCreateDir)
                .append("txt", txt)
                .append("txtSftp", txtSftp)
                .append("txtSftpOutput", txtSftpOutput)
                .append("txtOutput", txtOutput)
                .append("txtOutputAppend", txtOutputAppend)
                .append("txtOutputAutoCreateDir", txtOutputAutoCreateDir)
                .append("txtOutputCharset", txtOutputCharset)
                .append("txtOutputEOL", txtOutputEOL)
                .append("txtOutputEOF", txtOutputEOF)
                .append("txtSeparator", txtSeparator)
                .append("txtLengthMode", txtLengthMode)
                .append("txtFormat", txtFormat)
                .append("txtFormatDate", txtFormatDate)
                .append("txtFormatDatetime", txtFormatDatetime)
                .append("txtFillString", txtFillString)
                .append("txtFillNumber", txtFillNumber)
                .append("txtFillDate", txtFillDate)
                .append("csv", csv)
                .append("csvSftp", csvSftp)
                .append("csvSftpOutput", csvSftpOutput)
                .append("csvOutput", csvOutput)
                .append("csvOutputAppend", csvOutputAppend)
                .append("csvOutputAutoCreateDir", csvOutputAutoCreateDir)
                .append("csvOutputCharset", csvOutputCharset)
                .append("csvOutputBOF", csvOutputBOF)
                .append("csvOutputEOL", csvOutputEOL)
                .append("csvOutputEOF", csvOutputEOF)
                .append("csvHeader", csvHeader)
                .append("csvSeparator", csvSeparator)
                .append("csvNullString", csvNullString)
                .append("csvFormatDatetime", csvFormatDatetime)
                .append("csvFormatInteger", csvFormatInteger)
                .append("csvFormatDecimal", csvFormatDecimal)
                .append("csvFormatString", csvFormatString)
                .append("dbInsert", dbInsert)
                .append("dbInsertDataSource", dbInsertDataSource)
                .append("dbInsertColumnList", dbInsertColumnList)
                .append("dbInsertTable", dbInsertTable)
                .append("dbInsertNameQuotes", dbInsertNameQuotes)
                .append("dbInsertValueQuotes", dbInsertValueQuotes)
                .append("dbInsertPostSQL", dbInsertPostSQL)
                .append("dbInsertPreSQL", dbInsertPreSQL)
                .append("dbUpdate", dbUpdate)
                .append("dbUpdateDataSource", dbUpdateDataSource)
                .append("dbUpdateColumnList", dbUpdateColumnList)
                .append("dbUpdateTable", dbUpdateTable)
                .append("dbUpdateId", dbUpdateId)
                .append("dbUpdateNameQuotes", dbUpdateNameQuotes)
                .append("dbUpdateValueQuotes", dbUpdateValueQuotes)
                .append("dbUpdatePostSQL", dbUpdatePostSQL)
                .append("dbUpdatePreSQL", dbUpdatePreSQL)
                .append("dbExecute", dbExecute)
                .append("dbExecuteDataSource", dbExecuteDataSource)
                .append("dbExecuteColumn", dbExecuteColumn)
                .append("dbExecuteOutput", dbExecuteOutput)
                .append("dbExecutePostSQL", dbExecutePostSQL)
                .append("dbExecutePreSQL", dbExecutePreSQL)
                .append("outputTypeList", outputTypeList)
                .append("name", name)
                .append("valid", valid)
                .toString()
                .replace('=', ':');
    }

    public void setSrc(boolean src) {
        this.src = src;
    }

    public void setSrcSftp(String srcSftp) {
        this.srcSftp = srcSftp;
    }

    public void setSrcSftpOutput(String srcSftpOutput) {
        this.srcSftpOutput = srcSftpOutput;
    }

    public void setSrcOutput(String srcOutput) {
        this.srcOutput = srcOutput;
    }

    public void setSrcOutputAppend(boolean srcOutputAppend) {
        this.srcOutputAppend = srcOutputAppend;
    }

    public void setSrcOutputAutoCreateDir(boolean srcOutputAutoCreateDir) {
        this.srcOutputAutoCreateDir = srcOutputAutoCreateDir;
    }

    public void setSrcOutputCharset(String srcOutputCharset) {
        this.srcOutputCharset = srcOutputCharset;
    }

    public void setSrcOutputEOL(String srcOutputEOL) {
        this.srcOutputEOL = srcOutputEOL;
    }

    public void setSrcOutputEOF(String srcOutputEOF) {
        this.srcOutputEOF = srcOutputEOF;
    }

    public void setSrcOwner(String srcOwner) {
        this.srcOwner = srcOwner;
    }

    public void setSrcTable(String srcTable) {
        this.srcTable = srcTable;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public void setSrcDataSource(String srcDataSource) {
        this.srcDataSource = srcDataSource;
    }

    public void setSrcOutputs(String srcOutputs) {
        this.srcOutputs = srcOutputs;
    }

    public void setTar(boolean tar) {
        this.tar = tar;
    }

    public void setTarForSource(boolean tarForSource) {
        this.tarForSource = tarForSource;
    }

    public void setTarForName(boolean tarForName) {
        this.tarForName = tarForName;
    }

    public void setTarSftp(String tarSftp) {
        this.tarSftp = tarSftp;
    }

    public void setTarSftpOutput(String tarSftpOutput) {
        this.tarSftpOutput = tarSftpOutput;
    }

    public void setTarOutput(String tarOutput) {
        this.tarOutput = tarOutput;
    }

    public void setTarOutputAppend(boolean tarOutputAppend) {
        this.tarOutputAppend = tarOutputAppend;
    }

    public void setTarOutputAutoCreateDir(boolean tarOutputAutoCreateDir) {
        this.tarOutputAutoCreateDir = tarOutputAutoCreateDir;
    }

    public void setTarOutputCharset(String tarOutputCharset) {
        this.tarOutputCharset = tarOutputCharset;
    }

    public void setTarOutputEOL(String tarOutputEOL) {
        this.tarOutputEOL = tarOutputEOL;
    }

    public void setTarOutputEOF(String tarOutputEOF) {
        this.tarOutputEOF = tarOutputEOF;
    }

    public void setTarOutputs(String tarOutputs) {
        this.tarOutputs = tarOutputs;
    }

    public void setSql(boolean sql) {
        this.sql = sql;
    }

    public void setSqlSftp(String sqlSftp) {
        this.sqlSftp = sqlSftp;
    }

    public void setSqlSftpOutput(String sqlSftpOutput) {
        this.sqlSftpOutput = sqlSftpOutput;
    }

    public void setSqlCombineOutput(String sqlCombineOutput) {
        this.sqlCombineOutput = sqlCombineOutput;
    }

    public void setSqlOutput(String sqlOutput) {
        this.sqlOutput = sqlOutput;
    }

    public void setSqlOutputAppend(boolean sqlOutputAppend) {
        this.sqlOutputAppend = sqlOutputAppend;
    }

    public void setSqlOutputAutoCreateDir(boolean sqlOutputAutoCreateDir) {
        this.sqlOutputAutoCreateDir = sqlOutputAutoCreateDir;
    }

    public void setSqlOutputCharset(String sqlOutputCharset) {
        this.sqlOutputCharset = sqlOutputCharset;
    }

    public void setSqlOutputEOL(String sqlOutputEOL) {
        this.sqlOutputEOL = sqlOutputEOL;
    }

    public void setSqlOutputEOF(String sqlOutputEOF) {
        this.sqlOutputEOF = sqlOutputEOF;
    }

    public void setSqlTable(String sqlTable) {
        this.sqlTable = sqlTable;
    }

    public void setSqlColumn(List<String> sqlColumn) {
        this.sqlColumn = sqlColumn;
    }

    public void setSqlNameQuotes(String sqlNameQuotes) {
        this.sqlNameQuotes = sqlNameQuotes;
    }

    public void setSqlValueQuotes(String sqlValueQuotes) {
        this.sqlValueQuotes = sqlValueQuotes;
    }

    public void setSqlDBMS(String sqlDBMS) {
        this.sqlDBMS = sqlDBMS;
    }

    public void setSqlCreate(boolean sqlCreate) {
        this.sqlCreate = sqlCreate;
    }

    public void setSqlInsert(boolean sqlInsert) {
        this.sqlInsert = sqlInsert;
    }

    public void setSqlUpdate(boolean sqlUpdate) {
        this.sqlUpdate = sqlUpdate;
    }

    public void setSqlPostSQL(List<String> sqlPostSQL) {
        this.sqlPostSQL = sqlPostSQL;
    }

    public void setSqlPreSQL(List<String> sqlPreSQL) {
        this.sqlPreSQL = sqlPreSQL;
    }

    public void setMarkdown(boolean markdown) {
        this.markdown = markdown;
    }

    public void setMarkdownSftp(String markdownSftp) {
        this.markdownSftp = markdownSftp;
    }

    public void setMarkdownSftpOutput(String markdownSftpOutput) {
        this.markdownSftpOutput = markdownSftpOutput;
    }

    public void setMarkdownOutput(String markdownOutput) {
        this.markdownOutput = markdownOutput;
    }

    public void setMarkdownOutputAppend(boolean markdownOutputAppend) {
        this.markdownOutputAppend = markdownOutputAppend;
    }

    public void setMarkdownOutputAutoCreateDir(boolean markdownOutputAutoCreateDir) {
        this.markdownOutputAutoCreateDir = markdownOutputAutoCreateDir;
    }

    public void setMarkdownOutputCharset(String markdownOutputCharset) {
        this.markdownOutputCharset = markdownOutputCharset;
    }

    public void setMarkdownOutputEOL(String markdownOutputEOL) {
        this.markdownOutputEOL = markdownOutputEOL;
    }

    public void setMarkdownOutputEOF(String markdownOutputEOF) {
        this.markdownOutputEOF = markdownOutputEOF;
    }

    public void setMarkdownComment(boolean markdownComment) {
        this.markdownComment = markdownComment;
    }

    public void setMarkdownCommentDataSource(boolean markdownCommentDataSource) {
        this.markdownCommentDataSource = markdownCommentDataSource;
    }

    public void setMarkdownCommentQuery(boolean markdownCommentQuery) {
        this.markdownCommentQuery = markdownCommentQuery;
    }

    public void setMarkdownTitle(boolean markdownTitle) {
        this.markdownTitle = markdownTitle;
    }

    public void setMarkdownRowNumber(boolean markdownRowNumber) {
        this.markdownRowNumber = markdownRowNumber;
    }

    public void setMarkdownMermaid(boolean markdownMermaid) {
        this.markdownMermaid = markdownMermaid;
    }

    public void setMarkdownMermaidFull(boolean markdownMermaidFull) {
        this.markdownMermaidFull = markdownMermaidFull;
    }

    public void setPdf(boolean pdf) {
        this.pdf = pdf;
    }

    public void setPdfJRXML(Object pdfJRXML) {
        this.pdfJRXML = pdfJRXML;
    }

    public void setPdfSftp(String pdfSftp) {
        this.pdfSftp = pdfSftp;
    }

    public void setPdfSftpOutput(String pdfSftpOutput) {
        this.pdfSftpOutput = pdfSftpOutput;
    }

    public void setPdfOutput(String pdfOutput) {
        this.pdfOutput = pdfOutput;
    }

    public void setPdfOutputAutoCreateDir(boolean pdfOutputAutoCreateDir) {
        this.pdfOutputAutoCreateDir = pdfOutputAutoCreateDir;
    }

    public void setTxt(boolean txt) {
        this.txt = txt;
    }

    public void setTxtSftp(String txtSftp) {
        this.txtSftp = txtSftp;
    }

    public void setTxtSftpOutput(String txtSftpOutput) {
        this.txtSftpOutput = txtSftpOutput;
    }

    public void setTxtOutput(String txtOutput) {
        this.txtOutput = txtOutput;
    }

    public void setTxtOutputAppend(boolean txtOutputAppend) {
        this.txtOutputAppend = txtOutputAppend;
    }

    public void setTxtOutputAutoCreateDir(boolean txtOutputAutoCreateDir) {
        this.txtOutputAutoCreateDir = txtOutputAutoCreateDir;
    }

    public void setTxtOutputCharset(String txtOutputCharset) {
        this.txtOutputCharset = txtOutputCharset;
    }

    public void setTxtOutputEOL(String txtOutputEOL) {
        this.txtOutputEOL = txtOutputEOL;
    }

    public void setTxtOutputEOF(String txtOutputEOF) {
        this.txtOutputEOF = txtOutputEOF;
    }

    public void setTxtSeparator(String txtSeparator) {
        this.txtSeparator = txtSeparator;
    }

    public void setTxtLengthMode(String txtLengthMode) {
        this.txtLengthMode = txtLengthMode;
    }

    public void setTxtFormat(List<String> txtFormat) {
        this.txtFormat = txtFormat;
    }

    public void setTxtFormatDate(String txtFormatDate) {
        this.txtFormatDate = txtFormatDate;
    }

    public void setTxtFormatDatetime(String txtFormatDatetime) {
        this.txtFormatDatetime = txtFormatDatetime;
    }

    public void setTxtFillString(String txtFillString) {
        this.txtFillString = txtFillString;
    }

    public void setTxtFillNumber(String txtFillNumber) {
        this.txtFillNumber = txtFillNumber;
    }

    public void setTxtFillDate(String txtFillDate) {
        this.txtFillDate = txtFillDate;
    }

    public void setCsv(boolean csv) {
        this.csv = csv;
    }

    public void setCsvSftp(String csvSftp) {
        this.csvSftp = csvSftp;
    }

    public void setCsvSftpOutput(String csvSftpOutput) {
        this.csvSftpOutput = csvSftpOutput;
    }

    public void setCsvOutput(String csvOutput) {
        this.csvOutput = csvOutput;
    }

    public void setCsvOutputAppend(boolean csvOutputAppend) {
        this.csvOutputAppend = csvOutputAppend;
    }

    public void setCsvOutputAutoCreateDir(boolean csvOutputAutoCreateDir) {
        this.csvOutputAutoCreateDir = csvOutputAutoCreateDir;
    }

    public void setCsvOutputCharset(String csvOutputCharset) {
        this.csvOutputCharset = csvOutputCharset;
    }

    public void setCsvOutputBOF(String csvOutputBOF) {
        this.csvOutputBOF = csvOutputBOF;
    }

    public void setCsvOutputEOL(String csvOutputEOL) {
        this.csvOutputEOL = csvOutputEOL;
    }

    public void setCsvOutputEOF(String csvOutputEOF) {
        this.csvOutputEOF = csvOutputEOF;
    }

    public void setCsvHeader(boolean csvHeader) {
        this.csvHeader = csvHeader;
    }

    public void setCsvHeaderColumn(List<String> csvHeaderColumn) {
        this.csvHeaderColumn = csvHeaderColumn;
    }

    public void setCsvSeparator(String csvSeparator) {
        this.csvSeparator = csvSeparator;
    }

    public void setCsvNullString(String csvNullString) {
        this.csvNullString = csvNullString;
    }

    public void setCsvFormat(List<String> csvFormat) {
        this.csvFormat = csvFormat;
    }

    public void setCsvFormatDate(String csvFormatDate) {
        this.csvFormatDate = csvFormatDate;
    }

    public void setCsvFormatDatetime(String csvFormatDatetime) {
        this.csvFormatDatetime = csvFormatDatetime;
    }

    public void setCsvFormatInteger(String csvFormatInteger) {
        this.csvFormatInteger = csvFormatInteger;
    }

    public void setCsvFormatDecimal(String csvFormatDecimal) {
        this.csvFormatDecimal = csvFormatDecimal;
    }

    public void setCsvFormatString(String csvFormatString) {
        this.csvFormatString = csvFormatString;
    }

    public void setDbInsert(boolean dbInsert) {
        this.dbInsert = dbInsert;
    }

    public void setDbInsertDataSource(String dbInsertDataSource) {
        this.dbInsertDataSource = dbInsertDataSource;
    }

    public void setDbInsertColumnList(List<String> dbInsertColumnList) {
        this.dbInsertColumnList = dbInsertColumnList;
    }

    public void setDbInsertTable(String dbInsertTable) {
        this.dbInsertTable = dbInsertTable;
    }

    public void setDbInsertNameQuotes(String dbInsertNameQuotes) {
        this.dbInsertNameQuotes = dbInsertNameQuotes;
    }

    public void setDbInsertValueQuotes(String dbInsertValueQuotes) {
        this.dbInsertValueQuotes = dbInsertValueQuotes;
    }

    public void setDbInsertPostSQL(List<String> dbInsertPostSQL) {
        this.dbInsertPostSQL = dbInsertPostSQL;
    }

    public void setDbInsertPreSQL(List<String> dbInsertPreSQL) {
        this.dbInsertPreSQL = dbInsertPreSQL;
    }

    public void setDbUpdate(boolean dbUpdate) {
        this.dbUpdate = dbUpdate;
    }

    public void setDbUpdateDataSource(String dbUpdateDataSource) {
        this.dbUpdateDataSource = dbUpdateDataSource;
    }

    public void setDbUpdateColumnList(List<String> dbUpdateColumnList) {
        this.dbUpdateColumnList = dbUpdateColumnList;
    }

    public void setDbUpdateTable(String dbUpdateTable) {
        this.dbUpdateTable = dbUpdateTable;
    }

    public void setDbUpdateId(String dbUpdateId) {
        this.dbUpdateId = dbUpdateId;
    }

    public void setDbUpdateNameQuotes(String dbUpdateNameQuotes) {
        this.dbUpdateNameQuotes = dbUpdateNameQuotes;
    }

    public void setDbUpdateValueQuotes(String dbUpdateValueQuotes) {
        this.dbUpdateValueQuotes = dbUpdateValueQuotes;
    }

    public void setDbUpdatePostSQL(List<String> dbUpdatePostSQL) {
        this.dbUpdatePostSQL = dbUpdatePostSQL;
    }

    public void setDbUpdatePreSQL(List<String> dbUpdatePreSQL) {
        this.dbUpdatePreSQL = dbUpdatePreSQL;
    }

    public void setDbExecute(boolean dbExecute) {
        this.dbExecute = dbExecute;
    }

    public void setDbExecuteDataSource(String dbExecuteDataSource) {
        this.dbExecuteDataSource = dbExecuteDataSource;
    }

    public void setDbExecuteColumn(String dbExecuteColumn) {
        this.dbExecuteColumn = dbExecuteColumn;
    }

    public void setDbExecuteOutput(String dbExecuteOutput) {
        this.dbExecuteOutput = dbExecuteOutput;
    }

    public void setDbExecutePostSQL(List<String> dbExecutePostSQL) {
        this.dbExecutePostSQL = dbExecutePostSQL;
    }

    public void setDbExecutePreSQL(List<String> dbExecutePreSQL) {
        this.dbExecutePreSQL = dbExecutePreSQL;
    }

    public void setOsVariable(boolean osVariable) {
        this.osVariable = osVariable;
    }

    public void setOsVariableName(String osVariableName) {
        this.osVariableName = osVariableName;
    }

    public void setOsVariableValue(String osVariableValue) {
        this.osVariableValue = osVariableValue;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    public void setEmailSftp(String emailSftp) {
        this.emailSftp = emailSftp;
    }

    public void setEmailSftpOutput(String emailSftpOutput) {
        this.emailSftpOutput = emailSftpOutput;
    }

    public void setEmailOutput(String emailOutput) {
        this.emailOutput = emailOutput;
    }

    public void setEmailOutputAppend(boolean emailOutputAppend) {
        this.emailOutputAppend = emailOutputAppend;
    }

    public void setEmailOutputAutoCreateDir(boolean emailOutputAutoCreateDir) {
        this.emailOutputAutoCreateDir = emailOutputAutoCreateDir;
    }

    public void setEmailOutputCharset(String emailOutputCharset) {
        this.emailOutputCharset = emailOutputCharset;
    }

    public void setEmailOutputEOL(String emailOutputEOL) {
        this.emailOutputEOL = emailOutputEOL;
    }

    public void setEmailOutputEOF(String emailOutputEOF) {
        this.emailOutputEOF = emailOutputEOF;
    }

    public void setEmailComment(boolean emailComment) {
        this.emailComment = emailComment;
    }

    public void setEmailCommentDataSource(boolean emailCommentDataSource) {
        this.emailCommentDataSource = emailCommentDataSource;
    }

    public void setEmailCommentQuery(boolean emailCommentQuery) {
        this.emailCommentQuery = emailCommentQuery;
    }

    public void setEmailSMTP(String emailSMTP) {
        this.emailSMTP = emailSMTP;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public void setEmailCC(String emailCC) {
        this.emailCC = emailCC;
    }

    public void setEmailBCC(String emailBCC) {
        this.emailBCC = emailBCC;
    }

    public void setEmailHtml(boolean emailHtml) {
        this.emailHtml = emailHtml;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public void setOutputTypeList(List<OutputTypes> outputTypeList) {
        this.outputTypeList = outputTypeList;
    }

    public void setOutputPluginConfigMap(HashMap<String, OutputPluginConfig> outputPluginConfigMap) {
        this.outputPluginConfigMap = outputPluginConfigMap;
    }

    @Override
    public void saveProperties() throws ConfigurationException {
        if (src) saveSourceProperties();
        if (tar) saveTargetProperties();
        if (sql) saveSQLProperties();
        if (markdown) saveMarkdownProperties();
        if (pdf) savePDFProperties();
        if (txt) saveTXTProperties();
        if (csv) saveCSVProperties();
        if (dbInsert) saveDBInsertProperties();
        if (dbUpdate) saveDBUpdateProperties();
        if (dbExecute) saveDBExecuteProperties();
        if (osVariable) saveOSVariableProperties();
        if (email) saveEmailProperties();

    }

    private void saveEmailProperties() {
        String name = Property.EMAIL.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, email);

        setPropertyString(properties, Property.SFTP.prefixKey(name), null, emailSftp);
        setPropertyString(properties, Property.connectKeyString(name, Property.SFTP.connectKey(Property.OUTPUT_FILE)), null, emailSftpOutput);
        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "$[CAL:NAME(current)].md", emailOutput);
        setPropertyBoolean(properties, Property.OUTPUT_APPEND.prefixKey(name), false, emailOutputAppend);
        setPropertyBoolean(properties, Property.OUTPUT_AUTOCREATEDIR.prefixKey(name), true, emailOutputAutoCreateDir);
        setPropertyString(properties, Property.OUTPUT_CHARSET.prefixKey(name), "UTF-8", emailOutputCharset);
        setPropertyString(properties, Property.OUTPUT_EOL.prefixKey(name), "\n", emailOutputEOL);
        setPropertyString(properties, Property.OUTPUT_EOF.prefixKey(name), "\n", emailOutputEOF);

        setPropertyBoolean(properties, Property.COMMENT.prefixKey(name), true, emailComment);
        setPropertyBoolean(properties, Property.connectKeyString(name, Property.COMMENT.connectKey(Property.DATA_SOURCE)), true, emailCommentDataSource);
        setPropertyBoolean(properties, Property.connectKeyString(name, Property.COMMENT.connectKey(Property.QUERY)), true, emailCommentQuery);

        setPropertyString(properties, Property.SMTP.prefixKey(name), null, emailSMTP);
        setPropertyString(properties, Property.SUBJECT.prefixKey(name), null, emailSubject);
        setPropertyString(properties, Property.FROM.prefixKey(name), null, emailFrom);
        setPropertyString(properties, Property.TO.prefixKey(name), null, emailTo);
        setPropertyString(properties, Property.CC.prefixKey(name), null, emailCC);
        setPropertyString(properties, Property.BCC.prefixKey(name), null, emailBCC);
        setPropertyBoolean(properties, Property.HTML.prefixKey(name), false, emailHtml);
        setPropertyString(properties, Property.CONTENT.prefixKey(name), null, emailContent);
    }

    private void saveOSVariableProperties() {
        String name = Property.OSVARIABLE.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, osVariable);

        setPropertyString(properties, Property.NAME.prefixKey(name), "VARIABLE", osVariableName);
        setPropertyString(properties, Property.VALUE.prefixKey(name), "VALUE", osVariableValue);
    }

    private void saveDBExecuteProperties() {
        String name = Property.DBEXECUTE.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, dbExecute);

        setPropertyString(properties, Property.DATA_SOURCE.prefixKey(name), "", dbExecuteDataSource);
        setPropertyString(properties, Property.COLUMN.prefixKey(name), "sql", dbExecuteColumn);
        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "dbexecute_history_$[CAL:NAME(current)].log", dbExecuteOutput);

        for (String sql : dbExecutePreSQL) addPropertyString(properties, Property.PRE_SQL.prefixKey(name), sql, sql);
        for (String sql : dbExecutePostSQL) addPropertyString(properties, Property.POST_SQL.prefixKey(name), sql, sql);
    }

    private void saveDBUpdateProperties() {
        String name = Property.DBUPDATE.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, dbUpdate);

        setPropertyString(properties, Property.DATA_SOURCE.prefixKey(name), "", dbUpdateDataSource);

        for (String column : dbUpdateColumnList) addPropertyString(properties, Property.COLUMN.prefixKey(name), "", column);

        setPropertyString(properties, Property.TABLE.prefixKey(name), this.name, dbUpdateTable);
        setPropertyString(properties, Property.ID.prefixKey(name), dbUpdateId, dbUpdateId);
        setPropertyString(properties, Property.connectKeyString(name, Property.QUOTES.connectKey(Property.NAME)), "", dbUpdateNameQuotes);
        setPropertyString(properties, Property.connectKeyString(name, Property.QUOTES.connectKey(Property.VALUE)), "\"", dbUpdateValueQuotes);

        for (String sql : dbUpdatePreSQL) addPropertyString(properties, Property.PRE_SQL.prefixKey(name), "", sql);
        for (String sql : dbUpdatePostSQL) addPropertyString(properties, Property.POST_SQL.prefixKey(name), "", sql);
    }

    private void saveDBInsertProperties() {
        String name = Property.DBINSERT.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, dbInsert);

        setPropertyString(properties, Property.DATA_SOURCE.prefixKey(name), "", dbInsertDataSource);

        for (String column : dbInsertColumnList) addPropertyString(properties, Property.COLUMN.prefixKey(name), column, column);

        setPropertyString(properties, Property.TABLE.prefixKey(name), this.name, dbInsertTable);
        setPropertyString(properties, Property.connectKeyString(name, Property.QUOTES.connectKey(Property.NAME)), "", dbInsertNameQuotes);
        setPropertyString(properties, Property.connectKeyString(name, Property.QUOTES.connectKey(Property.VALUE)), "\"", dbInsertValueQuotes);

        for (String sql : dbInsertPreSQL) addPropertyString(properties, Property.connectKeyString(name, Property.PRE_SQL.connectKey(Property.VALUE)), sql, sql);
        for (String sql : dbInsertPostSQL) addPropertyString(properties, Property.connectKeyString(name, Property.POST_SQL.connectKey(Property.VALUE)), sql, sql);
    }

    private void saveCSVProperties() {
        String name = Property.CSV.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, csv);

        setPropertyString(properties, Property.SFTP.prefixKey(name), null, csvSftp);
        setPropertyString(properties, Property.connectKeyString(name, Property.SFTP.connectKey(Property.OUTPUT_FILE)), null, csvSftpOutput);
        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "$[CAL:NAME(current)].csv", csvOutput);
        setPropertyBoolean(properties, Property.OUTPUT_APPEND.prefixKey(name), false, csvOutputAppend);
        setPropertyBoolean(properties, Property.OUTPUT_AUTOCREATEDIR.prefixKey(name), true, csvOutputAutoCreateDir);
        setPropertyString(properties, Property.OUTPUT_CHARSET.prefixKey(name), "UTF-8", csvOutputCharset);
        setPropertyString(properties, Property.OUTPUT_BOF.prefixKey(name), "", csvOutputBOF);
        setPropertyString(properties, Property.OUTPUT_EOL.prefixKey(name), "\n", csvOutputEOL);
        setPropertyString(properties, Property.OUTPUT_EOF.prefixKey(name), "\n", csvOutputEOF);
        setPropertyString(properties, Property.SEPARATOR.prefixKey(name), ",", csvSeparator);
        setPropertyString(properties, Property.NULL.prefixKey(name), "", csvNullString);
        setPropertyBoolean(properties, Property.HEADER.prefixKey(name), true, csvHeader);

        for (String column : csvHeaderColumn) addPropertyString(properties, Property.HEADER.prefixKey(name), column, column);
        for (String format : csvFormat) addPropertyString(properties, Property.FORMAT.prefixKey(name), format, format);

        setPropertyString(properties, Property.FORMAT_DATE.prefixKey(name), "dd/MM/yyyy", csvFormatDate);
        setPropertyString(properties, Property.FORMAT_DATETIME.prefixKey(name), "dd/MM/yyyy HH:mm:ss", csvFormatDatetime);
        setPropertyString(properties, Property.FORMAT_INTEGER.prefixKey(name), "###0", csvFormatInteger);
        setPropertyString(properties, Property.FORMAT_DECIMAL.prefixKey(name), "###0.####", csvFormatDecimal);
        setPropertyString(properties, Property.FORMAT_STRING.prefixKey(name), "", csvFormatString);
    }

    private void saveTXTProperties() {
        String name = Property.TXT.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, txt);

        setPropertyString(properties, Property.SFTP.prefixKey(name), null, txtSftp);
        setPropertyString(properties, Property.connectKeyString(name, Property.SFTP.connectKey(Property.OUTPUT_FILE)), null, txtSftpOutput);
        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "$[CAL:NAME(current)].txt", txtOutput);
        setPropertyBoolean(properties, Property.OUTPUT_APPEND.prefixKey(name), false, txtOutputAppend);
        setPropertyBoolean(properties, Property.OUTPUT_AUTOCREATEDIR.prefixKey(name), true, txtOutputAutoCreateDir);
        setPropertyString(properties, Property.OUTPUT_CHARSET.prefixKey(name), "UTF-8", txtOutputCharset);
        setPropertyString(properties, Property.OUTPUT_EOL.prefixKey(name), "\n", txtOutputEOL);
        setPropertyString(properties, Property.OUTPUT_EOF.prefixKey(name), "\n", txtOutputEOF);
        setPropertyString(properties, Property.LENGTH_MODE.prefixKey(name), "char", txtLengthMode);
        setPropertyString(properties, Property.SEPARATOR.prefixKey(name), "", txtSeparator);

        if (txtFormat.size() == 0) setPropertyString(properties, Property.FORMAT.prefixKey(name), "STR:256", "STR:256");
        else for (String format : txtFormat) addPropertyString(properties, Property.FORMAT.prefixKey(name), "STR:256", format);

        setPropertyString(properties, Property.FORMAT_DATE.prefixKey(name), "yyyyMMdd", txtFormatDate);
        setPropertyString(properties, Property.FORMAT_DATETIME.prefixKey(name), "yyyyMMddHHmmss", txtFormatDatetime);
        setPropertyString(properties, Property.FILL_STRING.prefixKey(name), " ", txtFillString);
        setPropertyString(properties, Property.FILL_NUMBER.prefixKey(name), "0", txtFillNumber);
        setPropertyString(properties, Property.FILL_DATE.prefixKey(name), " ", txtFillDate);
    }

    private void savePDFProperties() {
        String name = Property.PDF_TABLE.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, pdf);

        setPropertyString(properties, Property.SFTP.prefixKey(name), null, pdfSftp);
        setPropertyString(properties, Property.connectKeyString(name, Property.SFTP.connectKey(Property.OUTPUT_FILE)), null, pdfSftpOutput);
        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "$[CAL:NAME(current)].pdf", pdfOutput);
        setPropertyBoolean(properties, Property.OUTPUT_AUTOCREATEDIR.prefixKey(name), true, pdfOutputAutoCreateDir);
        /*Notice: when you need PDF-Table again please add JRXML to Property or split PDF to Plugin*/
    }

    private void saveMarkdownProperties() {
        String name = Property.MARKDOWN.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, markdown);

        setPropertyString(properties, Property.SFTP.prefixKey(name), null, markdownSftp);
        setPropertyString(properties, Property.connectKeyString(name, Property.SFTP.connectKey(Property.OUTPUT_FILE)), null, markdownSftpOutput);
        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "$[CAL:NAME(current)].md", markdownOutput);
        setPropertyBoolean(properties, Property.OUTPUT_APPEND.prefixKey(name), false, markdownOutputAppend);
        setPropertyBoolean(properties, Property.OUTPUT_AUTOCREATEDIR.prefixKey(name), true, markdownOutputAutoCreateDir);
        setPropertyString(properties, Property.OUTPUT_CHARSET.prefixKey(name), "UTF-8", markdownOutputCharset);
        setPropertyString(properties, Property.OUTPUT_EOL.prefixKey(name), "\n", markdownOutputEOL);
        setPropertyString(properties, Property.OUTPUT_EOF.prefixKey(name), "\n", markdownOutputEOF);

        setPropertyBoolean(properties, Property.COMMENT.prefixKey(name), true, markdownComment);
        setPropertyBoolean(properties, Property.connectKeyString(name, Property.COMMENT.connectKey(Property.DATA_SOURCE)), true, markdownCommentDataSource);
        setPropertyBoolean(properties, Property.connectKeyString(name, Property.COMMENT.connectKey(Property.QUERY)), true, markdownCommentQuery);
        setPropertyBoolean(properties, Property.TITLE.prefixKey(name), true, markdownTitle);
        setPropertyBoolean(properties, Property.ROW_NUMBER.prefixKey(name), true, markdownRowNumber);
        setPropertyBoolean(properties, Property.MERMAID.prefixKey(name), true, markdownMermaid);
        setPropertyBoolean(properties, Property.connectKeyString(name, Property.MERMAID.connectKey(Property.FULL.key())), true, markdownMermaidFull);
    }

    private void saveSQLProperties() {
        String name = Property.SQL.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, sql);

        setPropertyString(properties, Property.SFTP.prefixKey(name), null, sqlSftp);
        setPropertyString(properties, Property.connectKeyString(name, Property.SFTP.connectKey(Property.OUTPUT_FILE)), null, sqlSftpOutput);
        setPropertyString(properties, Property.connectKeyString(name, Property.COMBINE.connectKey(Property.OUTPUT_FILE)), null, sqlCombineOutput);
        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "$[CAL:NAME(current)].sql", sqlOutput);
        setPropertyBoolean(properties, Property.OUTPUT_APPEND.prefixKey(name), false, sqlOutputAppend);
        setPropertyBoolean(properties, Property.OUTPUT_AUTOCREATEDIR.prefixKey(name), true, sqlOutputAutoCreateDir);
        setPropertyString(properties, Property.OUTPUT_CHARSET.prefixKey(name), "UTF-8", sqlOutputCharset);
        setPropertyString(properties, Property.OUTPUT_EOL.prefixKey(name), "\n", sqlOutputEOL);
        setPropertyString(properties, Property.OUTPUT_EOF.prefixKey(name), "\n", sqlOutputEOF);
        setPropertyString(properties, Property.TABLE.prefixKey(name), this.name, sqlTable);
        setPropertyString(properties, Property.DBMS.prefixKey(name), "MYSQL", sqlDBMS);

        for (String column : sqlColumn) addPropertyString(properties, Property.COLUMN.prefixKey(name), column, column);

        setPropertyString(properties, Property.connectKeyString(name, Property.QUOTES.connectKey(Property.NAME)), "", sqlNameQuotes);
        setPropertyString(properties, Property.connectKeyString(name, Property.QUOTES.connectKey(Property.VALUE)), "\"", sqlValueQuotes);
        setPropertyBoolean(properties, Property.CREATE.prefixKey(name), false, sqlCreate);
        setPropertyBoolean(properties, Property.INSERT.prefixKey(name), false, sqlInsert);
        setPropertyBoolean(properties, Property.UPDATE.prefixKey(name), false, sqlUpdate);

        for (String sql : sqlPreSQL) addPropertyString(properties, Property.PRE_SQL.prefixKey(name), sql, sql);
        for (String sql : sqlPostSQL) addPropertyString(properties, Property.POST_SQL.prefixKey(name), sql, sql);
    }

    private void saveTargetProperties() {
        String name = Property.TAR.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, tar);

        setPropertyString(properties, Property.OUTPUT_TYPES.prefixKey(name), "sql,md", tarOutputs);
        setPropertyBoolean(properties, Property.connectKeyString(name, Property.FOR.connectKey(Property.SOURCE)), false, tarForSource);
        setPropertyBoolean(properties, Property.connectKeyString(name, Property.FOR.connectKey(Property.NAME)), false, tarForName);
        setPropertyString(properties, Property.SFTP.prefixKey(name), null, tarSftp);
        setPropertyString(properties, Property.connectKeyString(name, Property.SFTP.connectKey(Property.OUTPUT_FILE)), null, tarSftpOutput);

        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "$[CAL:NAME(current)].conf", tarOutput);
        setPropertyBoolean(properties, Property.OUTPUT_APPEND.prefixKey(name), false, tarOutputAppend);
        setPropertyBoolean(properties, Property.OUTPUT_AUTOCREATEDIR.prefixKey(name), true, tarOutputAutoCreateDir);
        setPropertyString(properties, Property.OUTPUT_CHARSET.prefixKey(name), "UTF-8", tarOutputCharset);
        setPropertyString(properties, Property.OUTPUT_EOL.prefixKey(name), "\n", tarOutputEOL);
        setPropertyString(properties, Property.OUTPUT_EOF.prefixKey(name), "\n", tarOutputEOF);
    }

    private void saveSourceProperties() {
        String name = Property.SRC.prefixKey(this.name);
        setBlancLinesBefore(name, 1);
        setPropertyBoolean(properties, name, false, src);

        setPropertyString(properties, Property.OWNER.prefixKey(name), "OWNER", srcOwner);
        setPropertyString(properties, Property.TABLE.prefixKey(name), "TABLE_NAME", srcTable);
        setPropertyString(properties, Property.ID.prefixKey(name), "COLUMN_NAME", srcId);
        setPropertyString(properties, Property.DATA_SOURCE.prefixKey(name), "datasource-name", srcDataSource);
        setPropertyString(properties, Property.OUTPUT_TYPES.prefixKey(name), "sql,md", srcOutputs);
        setPropertyString(properties, Property.SFTP.prefixKey(name), null, srcSftp);
        setPropertyString(properties, Property.connectKeyString(name, Property.SFTP.connectKey(Property.OUTPUT_FILE)), null, srcSftpOutput);
        setPropertyString(properties, Property.OUTPUT_FILE.prefixKey(name), "$[CAL:NAME(current)].conf", srcOutput);

        setPropertyBoolean(properties, Property.OUTPUT_APPEND.prefixKey(name), false, srcOutputAppend);
        setPropertyBoolean(properties, Property.OUTPUT_AUTOCREATEDIR.prefixKey(name), true, srcOutputAutoCreateDir);

        setPropertyString(properties, Property.OUTPUT_CHARSET.prefixKey(name), "UTF-8", srcOutputCharset);
        setPropertyString(properties, Property.OUTPUT_EOL.prefixKey(name), "\n", srcOutputEOL);
        setPropertyString(properties, Property.OUTPUT_EOF.prefixKey(name), "\n", srcOutputEOF);
    }
}