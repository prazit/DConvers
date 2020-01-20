package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.output.OutputFactory;
import com.clevel.dconvers.output.OutputTypes;
import org.apache.commons.configuration2.Configuration;
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


    private List<OutputTypes> outputTypeList;

    /**
     * Map<Output Plugin Name, <? extends OutputPluginConfig>>
     **/
    private HashMap<String, OutputPluginConfig> outputPluginConfigMap;

    public OutputConfig(Application application, String baseProperty, Configuration baseProperties) {
        super(application, baseProperty);
        this.properties = baseProperties;
        outputPluginConfigMap = new HashMap<>();

        valid = loadProperties();
        if (valid) valid = validate();

        log.trace("OutputConfig({}) is created", name);
    }

    @Override
    protected boolean loadProperties() {
        String baseProperty = name;
        outputTypeList = new ArrayList<>();

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
            tarOutputs = getPropertyString(tarProperties, Property.TABLE.key(), tarOutputs);
            tarOutput = getPropertyString(tarProperties, Property.OUTPUT_FILE.key(), tarOutput);
            tarOutputAppend = tarProperties.getBoolean(Property.OUTPUT_APPEND.key(), tarOutputAppend);
            tarOutputAutoCreateDir = tarProperties.getBoolean(Property.OUTPUT_AUTOCREATEDIR.key(), tarOutputAutoCreateDir);
            tarOutputCharset = getPropertyString(tarProperties, Property.OUTPUT_CHARSET.key(), tarOutputCharset);
            tarOutputEOL = getPropertyString(tarProperties, Property.OUTPUT_EOL.key(), tarOutputEOL);
            tarOutputEOF = getPropertyString(tarProperties, Property.OUTPUT_EOF.key(), tarOutputEOF);
        }

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

        // Default Properties for PDF
        pdf = false;
        pdfSftp = null;
        pdfSftpOutput = null;
        pdfOutput = "$[CAL:NAME(current)].pdf";
        pdfOutputAutoCreateDir = true;
        pdfJRXML = "";

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
        txtFormat.add("STR:256");
        txtFormatDate = "yyyyMMdd";
        txtFormatDatetime = "yyyyMMddHHmmss";
        txtFillString = " ";
        txtFillNumber = "0";
        txtFillDate = " ";

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

        // DBInsert Output Properties
        dbInsert = false;
        dbInsertDataSource = "";
        dbInsertColumnList = new ArrayList<>();
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
            dbInsertDataSource = getPropertyString(dbInsertProperties, Property.DATA_SOURCE.key(), dbInsertDataSource);
            dbInsertColumnList = getStringList(dbInsertProperties, Property.COLUMN.key());
            dbInsertTable = getPropertyString(dbInsertProperties, Property.TABLE.key(), dbInsertTable);
            dbInsertNameQuotes = getPropertyString(dbInsertProperties, Property.QUOTES.connectKey(Property.NAME), dbInsertNameQuotes);
            dbInsertValueQuotes = getPropertyString(dbInsertProperties, Property.QUOTES.connectKey(Property.VALUE), dbInsertValueQuotes);
            dbInsertPreSQL = getSQLStringList(dbInsertProperties, Property.PRE_SQL.key());
            dbInsertPostSQL = getSQLStringList(dbInsertProperties, Property.POST_SQL.key());
        }

        // DBUpdate Output Properties
        dbUpdate = false;
        dbUpdateDataSource = "";
        dbUpdateColumnList = new ArrayList<>();
        dbUpdateTable = name;
        dbUpdateNameQuotes = "";
        dbUpdateValueQuotes = "\"";
        dbUpdateId = getPropertyString(properties, Property.ID.key(), "id");
        dbUpdatePreSQL = new ArrayList<>();
        dbUpdatePostSQL = new ArrayList<>();

        key = Property.DBUPDATE.prefixKey(baseProperty);
        dbUpdate = properties.getBoolean(key, dbUpdate);
        if (dbUpdate) {
            outputTypeList.add(OutputTypes.UPDATE_DB);

            Configuration dbUpdateProperties = properties.subset(key);
            dbUpdateDataSource = getPropertyString(dbUpdateProperties, Property.DATA_SOURCE.key(), dbUpdateDataSource);
            dbUpdateColumnList = getStringList(dbUpdateProperties, Property.TABLE.key());
            dbUpdateColumnList = getStringList(dbUpdateProperties, Property.COLUMN.key());
            dbUpdateTable = getPropertyString(dbUpdateProperties, Property.TABLE.key(), dbUpdateTable);
            dbUpdateId = dbUpdateProperties.getString(Property.ID.key(), dbUpdateId);
            dbUpdateNameQuotes = dbUpdateProperties.getString(Property.QUOTES.connectKey(Property.NAME), dbUpdateNameQuotes);
            dbUpdateValueQuotes = dbUpdateProperties.getString(Property.QUOTES.connectKey(Property.VALUE), dbUpdateValueQuotes);
            dbUpdatePreSQL = getSQLStringList(dbUpdateProperties, Property.PRE_SQL.key());
            dbUpdatePostSQL = getSQLStringList(dbUpdateProperties, Property.POST_SQL.key());
        }

        // DBExecute Output Properties
        dbExecute = false;
        dbExecuteDataSource = "";
        dbExecuteColumn = "sql";
        dbExecuteOutput = "dbexecute_history_$[CAL:NAME(current)].log";
        dbExecutePreSQL = new ArrayList<>();
        dbExecutePostSQL = new ArrayList<>();

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

        // OS Variable Output Properties
        osVariable = false;
        osVariableName = "VARIABLE";
        osVariableValue = "VALUE";

        key = Property.OSVARIABLE.prefixKey(baseProperty);
        osVariable = properties.getBoolean(key, osVariable);
        if (osVariable) {
            outputTypeList.add(OutputTypes.OS_VARIABLE);

            Configuration osVariableProperties = properties.subset(key);
            osVariableName = getPropertyString(osVariableProperties, Property.NAME.key(), osVariableName);
            osVariableValue = getPropertyString(osVariableProperties, Property.VALUE.key(), osVariableValue);
        }

        // Load Plugin Config into outputPluginConfigMap
        HashMap<String, Class> plugins = OutputTypes.getPlugins();
        log.debug("OutputConfig.loadProperties found {}", plugins.size());
        for (String pluginName : plugins.keySet()) {
            key = baseProperty + "." + pluginName;
            boolean enabled = properties.getBoolean(key, false);
            log.debug("OutputConfig.loadProperties.plugin={}, baseProperty={}, enabled={}", pluginName, baseProperty, enabled);
            if (enabled) {
                OutputTypes type = OutputTypes.parse(pluginName);
                outputTypeList.add(type);

                OutputPluginConfig pluginConfig = OutputFactory.getPluginConfig(application, type);
                pluginConfig.loadConfig(properties.subset(key));

                log.debug("OutputConfig.loadProperties.plugin({})={}", pluginName, pluginConfig.toString());
                outputPluginConfigMap.put(type.getName(), pluginConfig);
            }
        }
        log.debug("OutputTypeList(name:{})={}", name, outputTypeList.toString());

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
        return application.currentConverter.compileDynamicValues(sqlSftpOutput);
    }

    public String getSqlOutput() {
        return application.currentConverter.compileDynamicValues(sqlOutput);
    }

    public String getSqlCombineOutput() {
        return application.currentConverter.compileDynamicValues(sqlCombineOutput);
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
        return application.currentConverter.compileDynamicValues(sqlPostSQL);
    }

    public List<String> getSqlPreSQL() {
        return application.currentConverter.compileDynamicValues(sqlPreSQL);
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
        return application.currentConverter.compileDynamicValues(csvSftpOutput);
    }

    public String getCsvOutput() {
        return application.currentConverter.compileDynamicValues(csvOutput);
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
        return application.currentConverter.compileDynamicValues(csvOutputBOF);
    }

    public String getCsvOutputEOL() {
        return application.currentConverter.compileDynamicValues(csvOutputEOL);
    }

    public String getCsvOutputEOF() {
        return application.currentConverter.compileDynamicValues(csvOutputEOF);
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
        return application.currentConverter.compileDynamicValues(dbInsertPostSQL);
    }

    public List<String> getDbInsertPreSQL() {
        return application.currentConverter.compileDynamicValues(dbInsertPreSQL);
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
        return application.currentConverter.compileDynamicValues(dbUpdatePostSQL);
    }

    public List<String> getDbUpdatePreSQL() {
        return application.currentConverter.compileDynamicValues(dbUpdatePreSQL);
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
        return application.currentConverter.compileDynamicValues(dbExecuteOutput);
    }

    public List<String> getDbExecutePostSQL() {
        return application.currentConverter.compileDynamicValues(dbExecutePostSQL);
    }

    public List<String> getDbExecutePreSQL() {
        return application.currentConverter.compileDynamicValues(dbExecutePreSQL);
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

}