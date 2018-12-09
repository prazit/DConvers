package com.clevel.dconvers.ngin.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.conf.SystemVariable;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.Target;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.DataFormatter;
import com.clevel.dconvers.ngin.format.MarkdownFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MarkdownOutput extends Output {

    public MarkdownOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        String eol = outputConfig.getMarkdownOutputEOL();
        String eof = outputConfig.getMarkdownOutputEOF();

        dataFormatterList.add(new MarkdownFormatter(application, name, eol, eof));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String converterName = application.currentConverter.getName();
        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;
        String outputPath;
        String headPrint = null;
        Object owner = dataTable.getOwner();

        if (owner instanceof Source) {
            Source source = (Source) owner;
            outputPath = dataConversionConfigFile.getOutputSourcePath();
            headPrint = "\n"
                    + "> Generated by dconvers at " + application.getSystemVariableValue(SystemVariable.NOW) + ".  \n"
                    + "> This markdown file contains " + dataTable.getRowCount() + " rows from source(" + source.getName() + ") in converter(" + converterName + ")  \n"
                    + "> Query : " + dataTable.getQuery() + "  \n"
                    + "\n";
        } else if (owner instanceof Target) {
            Target target = (Target) owner;
            outputPath = dataConversionConfigFile.getOutputTargetPath();
            headPrint = "\n"
                    + "> Generated by dconvers at " + application.getSystemVariableValue(SystemVariable.NOW) + ".  \n"
                    + "> This markdown file contains " + dataTable.getRowCount() + " rows from target(" + target.getName() + ") in converter(" + converterName + ")  \n"
                    + "> Data from : source(" + target.getTargetConfig().getSource() + ")  \n"
                    + "\n";
        } else {
            outputPath = dataConversionConfigFile.getOutputMappingPath();
            headPrint = "";
        }

        String markdownOutputFilename = outputPath + outputConfig.getMarkdownOutput();
        Writer writer = createFile(markdownOutputFilename, outputConfig.isMarkdownOutputAutoCreateDir(), outputConfig.isMarkdownOutputAppend(), outputConfig.getMarkdownOutputCharset());
        if (headPrint != null && writer != null) {
            try {
                writer.write(headPrint);
            } catch (IOException e) {
                error("MarkdownOutput: write the head print failed, {}", e.getMessage());
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
