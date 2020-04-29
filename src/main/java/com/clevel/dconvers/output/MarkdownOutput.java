package com.clevel.dconvers.output;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.format.DataFormatter;
import com.clevel.dconvers.format.MarkdownFormatter;
import com.clevel.dconvers.input.DataSource;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.Target;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MarkdownOutput extends Output {

    public MarkdownOutput(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        String eol = outputConfig.getMarkdownOutputEOL();
        String eof = outputConfig.getMarkdownOutputEOF();
        boolean showTitle = outputConfig.isMarkdownTitle();
        boolean showRowNumber = outputConfig.isMarkdownRowNumber();
        boolean mermaid = outputConfig.isMarkdownMermaid();
        boolean mermaidFull = outputConfig.isMarkdownMermaidFull();

        dataFormatterList.add(new MarkdownFormatter(dconvers, name, eol, eof, showTitle, showRowNumber, mermaid, mermaidFull));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String converterName = dconvers.currentConverter.getName();
        DataConversionConfigFile dataConversionConfigFile = dconvers.dataConversionConfigFile;
        DynamicValueType tableType = dataTable.getTableType();
        Object owner = dataTable.getOwner();
        String eol = outputConfig.getMarkdownOutputEOL();

        String outputPath;
        String headPrint;
        log.debug("MarkdownOutput.openWriter. tableType is {}", tableType);
        if (DynamicValueType.SRC.equals(tableType)) {
            Source source = (Source) owner;
            outputPath = dataConversionConfigFile.getOutputSourcePath();
            String dataSourceName = dataTable.getDataSource();
            DataSource dataSource = dconvers.getDataSource(dataSourceName);
            headPrint = eol
                    + "> Generated at " + dconvers.getSystemVariableValue(SystemVariable.NOW) + ".  " + eol
                    + "> Using " + dconvers.getSystemVariableValue(SystemVariable.APPLICATION_FULL_VERSION) + ".  " + eol
                    + "> This markdown table contains " + dataTable.getRowCount() + " rows from source(" + source.getName() + ") in converter(" + converterName + ")  " + eol
                    + (outputConfig.isMarkdownCommentDataSource() ? ("> DataSource : " + (dataSource == null ? "null" : dataSource.toString()) + eol) : "");
            if (dataSource != null && dataSource.getDataSourceConfig().getDbms() != null) {
                if (outputConfig.isMarkdownCommentQuery()) {
                    headPrint += "> Query : " + eol
                            + "```sql" + eol
                            + dataTable.getQuery() + eol
                            + "```" + eol;
                }
            } else {
                headPrint += (outputConfig.isMarkdownCommentQuery() ? ("> Query : " + dataTable.getQuery() + eol) : "")
                        + eol;
            }

        } else if (DynamicValueType.TAR.equals(tableType)) {
            Target target = (Target) owner;
            outputPath = dataConversionConfigFile.getOutputTargetPath();
            headPrint = eol
                    + "> Generated at " + dconvers.getSystemVariableValue(SystemVariable.NOW) + ".  " + eol
                    + "> Using " + dconvers.getSystemVariableValue(SystemVariable.APPLICATION_FULL_VERSION) + ".  " + eol
                    + "> This markdown table contains " + dataTable.getRowCount() + " rows from target(" + target.getName() + ") in converter(" + converterName + ")  " + eol
                    + (outputConfig.isMarkdownCommentQuery() ? ("> Data from : source(" + target.getTargetConfig().getSource() + ")  " + eol) : "")
                    + eol;

        } else if (DynamicValueType.MAP.equals(tableType)) {
            Pair<DataTable, DataTable> sourceToTarget = (Pair<DataTable, DataTable>) owner;
            outputPath = dataConversionConfigFile.getOutputMappingPath();
            if (sourceToTarget == null) {
                headPrint = eol
                        + "> Generated at " + dconvers.getSystemVariableValue(SystemVariable.NOW) + ".  " + eol
                        + "> Using " + dconvers.getSystemVariableValue(SystemVariable.APPLICATION_FULL_VERSION) + ".  " + eol
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
                headPrint = eol
                        + "> Generated at " + dconvers.getSystemVariableValue(SystemVariable.NOW) + "." + eol
                        + "> Using " + dconvers.getSystemVariableValue(SystemVariable.APPLICATION_FULL_VERSION) + ".  " + eol
                        + "> This markdown table contains " + dataTable.getRowCount() + " rows from mapping-table of target(" + target.getName() + ") in converter(" + converterName + ")" + eol
                        + (outputConfig.isMarkdownCommentQuery() ? ("> Data from : Target(" + target.getName() + ").id(" + targetTable.getIdColumnName() + ") as " + Property.TARGET_ID.key() + ", " + sourceName + ".id(" + sourceTable.getIdColumnName() + ") as " + Property.SOURCE_ID.key() + eol) : "")
                        + eol;
            }

        } else {
            headPrint = null;
            outputPath = "";
        }

        String markdownOutputFilename = outputPath + outputConfig.getMarkdownOutput();
        Writer writer = createFile(markdownOutputFilename, outputConfig.isMarkdownOutputAutoCreateDir(), outputConfig.isMarkdownOutputAppend(), outputConfig.getMarkdownOutputCharset());
        if (headPrint != null && writer != null) {
            try {
                writer.write(headPrint);
            } catch (IOException e) {
                error("MarkdownOutput: write the head print failed", e);
                return null;
            }
        }
        registerPostSFTP(markdownOutputFilename, outputConfig.getMarkdownSftpOutput(), outputConfig.getMarkdownSftp());

        return writer;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(MarkdownOutput.class);
    }
}
