package com.clevel.dconvers.output;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.format.DataFormatter;
import com.clevel.dconvers.format.SQLCreateFormatter;
import com.clevel.dconvers.format.SQLInsertFormatter;
import com.clevel.dconvers.format.SQLUpdateFormatter;
import com.clevel.dconvers.input.DataSource;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.Target;
import com.clevel.dconvers.ngin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class SQLOutput extends Output {

    private List<DataFormatter> dataFormatterList;
    private Writer combineWriter;

    public SQLOutput(DConvers dconvers, String name) {
        super(dconvers, name);
        combineWriter = null;
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        dataFormatterList = new ArrayList<>();
        List<String> columnList = outputConfig.getSqlColumn();
        String dbms = outputConfig.getSqlDBMS();
        String tableName = outputConfig.getSqlTable();
        String nameQuotes = outputConfig.getSqlNameQuotes();
        String valueQuotes = outputConfig.getSqlValueQuotes();
        String eol = outputConfig.getSqlOutputEOL();

        boolean generateUpdateStatement = outputConfig.isSqlUpdate();
        boolean generateInsertStatement = outputConfig.isSqlInsert();
        boolean generateCreateStatement = outputConfig.isSqlCreate();

        if (generateCreateStatement) {
            dataFormatterList.add(new SQLCreateFormatter(dconvers, tableName, dbms, nameQuotes, eol, generateInsertStatement && generateUpdateStatement));
        }
        if (generateInsertStatement) {
            dataFormatterList.add(new SQLInsertFormatter(dconvers, tableName, dbms, columnList, nameQuotes, valueQuotes, eol));
        }
        if (generateUpdateStatement) {
            dataFormatterList.add(new SQLUpdateFormatter(dconvers, tableName, dbms, columnList, nameQuotes, valueQuotes, eol));
        }

        log.debug("SQLOutput.formatterEnabled(create:{}, insert:{}, update:{})", generateCreateStatement, generateInsertStatement, generateUpdateStatement);
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String converterName = dconvers.currentConverter.getName();
        DataConversionConfigFile dataConversionConfigFile = dconvers.dataConversionConfigFile;
        DynamicValueType tableType = dataTable.getTableType();
        Object owner = dataTable.getOwner();
        String eol = outputConfig.getSqlOutputEOL();

        String outputPath;
        String headPrint;
        if (DynamicValueType.SRC.equals(tableType)) {
            Source source = (Source) owner;
            outputPath = dataConversionConfigFile.getOutputSourcePath();
            String dataSourceName = dataTable.getDataSource();
            DataSource dataSource = dconvers.getDataSource(dataSourceName);
            headPrint = "--" + eol
                    + "-- Generated by dconvers at " + dconvers.getSystemVariableValue(SystemVariable.NOW) + "." + eol
                    + "-- This sql file contains " + dataTable.getRowCount() + " rows from source(" + source.getName() + ") in converter(" + converterName + ")" + eol
                    + "-- DataSource : " + (dataSource == null ? "null" : dataSource.toString()) + eol
                    + "-- Query : " + dataTable.getQuery() + eol
                    + "--" + eol;

        } else if (DynamicValueType.TAR.equals(tableType)) {
            Target target = (Target) owner;
            outputPath = dataConversionConfigFile.getOutputTargetPath();
            headPrint = "--" + eol
                    + "-- Generated by dconvers at " + dconvers.getSystemVariableValue(SystemVariable.NOW) + "." + eol
                    + "-- This sql file contains " + dataTable.getRowCount() + " rows from target(" + target.getName() + ") in converter(" + converterName + ")" + eol
                    + "-- Data from : source(" + target.getTargetConfig().getSource() + ")" + eol
                    + "--" + eol;

        } else if (DynamicValueType.MAP.equals(tableType)) {
            Pair<DataTable, DataTable> sourceToTarget = (Pair<DataTable, DataTable>) owner;
            outputPath = dataConversionConfigFile.getOutputMappingPath();
            if (sourceToTarget == null) {
                headPrint = eol
                        + "> Generated by dconvers at " + dconvers.getSystemVariableValue(SystemVariable.NOW) + ".  " + eol
                        + "> Mapping Table with unknown owner(null)" + eol
                        + eol;
            } else {
                DataTable sourceTable = sourceToTarget.getKey();
                DataTable targetTable = sourceToTarget.getValue();
                Target target = (Target) targetTable.getOwner();
                String sourceName;
                if (sourceTable.getOwner() instanceof Source) {
                    Source source = (Source) sourceTable.getOwner();
                    sourceName = "Source(" + source.getName() + ")";
                } else {
                    Target source = (Target) sourceTable.getOwner();
                    sourceName = "Target(" + source.getName() + ")";
                }
                headPrint = "--" + eol
                        + "-- Generated by dconvers at " + dconvers.getSystemVariableValue(SystemVariable.NOW) + "." + eol
                        + "-- This SQL file contains " + dataTable.getRowCount() + " rows from mapping-table of target(" + target.getName() + ") in converter(" + converterName + ")" + eol
                        + "-- Data from : Target(" + target.getName() + ").id(" + targetTable.getIdColumnName() + "), " + sourceName + ".id(" + sourceTable.getIdColumnName() + ")" + eol
                        + "--" + eol;
            }

        } else {
            headPrint = null;
            outputPath = "";
        }

        String sqlOutputFilename = outputPath + outputConfig.getSqlOutput();
        Writer writer = createFile(sqlOutputFilename, outputConfig.isSqlOutputAutoCreateDir(), outputConfig.isSqlOutputAppend(), outputConfig.getSqlOutputCharset());
        if (headPrint != null && writer != null) {
            try {
                writer.write(headPrint);
            } catch (IOException e) {
                error("SQLOutput: write the head print failed, {}", e.getMessage());
                return null;
            }
        }
        registerPostSFTP(sqlOutputFilename, outputConfig.getSqlSftpOutput(), outputConfig.getSqlSftp());

        String sqlCombineFilename = outputConfig.getSqlCombineOutput();
        if (sqlCombineFilename != null) {
            sqlCombineFilename = outputPath + sqlCombineFilename;
            combineWriter = createFile(sqlCombineFilename, outputConfig.isSqlOutputAutoCreateDir(), true, outputConfig.getSqlOutputCharset());
            if (combineWriter != null) {
                for (DataFormatter dataFormatter : dataFormatterList) {
                    dataFormatter.addMoreWriter(combineWriter);
                }

                if (headPrint != null) {
                    try {
                        combineWriter.write(headPrint);
                    } catch (IOException e) {
                        error("SQLOutput: write the head print to combine-file is failed, {}", e.getMessage());
                        return null;
                    }
                }
            }
        }

        List<String> preSQL = outputConfig.getSqlPreSQL();
        if (preSQL.size() > 0) {
            try {
                for (String sql : preSQL) {
                    writer.write(sql + eol);
                    if (combineWriter != null) {
                        combineWriter.write(sql + eol);
                    }
                }
            } catch (IOException e) {
                error("SQLOutput: write the pre-sql failed, {}", e.getMessage());
                return null;
            }
        }

        return writer;
    }

    @Override
    protected boolean closeWriter(OutputConfig outputConfig, DataTable dataTable, Writer writer, boolean success) {

        if (success) {

            String eol = outputConfig.getSqlOutputEOL();
            String eof = outputConfig.getSqlOutputEOF();
            List<String> postSQL = outputConfig.getSqlPostSQL();
            if (postSQL.size() > 0) {
                try {
                    for (String sql : postSQL) {
                        writer.write(sql + eol);
                        if (combineWriter != null) {
                            combineWriter.write(sql + eol);
                        }
                    }
                } catch (IOException e) {
                    error("SQLOutput: write the post-sql failed, {}", e.getMessage());
                    success = false;
                }
            }

            if (!eof.isEmpty()) {
                try {
                    writer.write(eof);
                    if (combineWriter != null) {
                        combineWriter.write(eof);
                    }
                } catch (IOException e) {
                    error("SQLOutput: write EOF({}) is failed, {}", eof, e);
                    success = false;
                }
            }

        } // end of if (success)

        try {
            writer.close();
            if (combineWriter != null) {
                combineWriter.close();
            }
        } catch (IOException e) {
            // do nothing
        } finally {
            combineWriter = null;
        }

        return success;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SQLOutput.class);
    }

}
