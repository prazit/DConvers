package com.clevel.dconvers.ngin.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.ConverterConfigFileFormatter;
import com.clevel.dconvers.ngin.format.DataFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ConfOutput extends Output {
    public ConfOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        dataFormatterList.add(new ConverterConfigFileFormatter(application, name,outputConfig));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String outputPath = getRootPath(dataTable);
        String confOutputFilename = outputPath + outputConfig.getConfOutput();

        Writer writer = createFile(confOutputFilename, outputConfig.isConfOutputAutoCreateDir(), outputConfig.isConfOutputAppend(), outputConfig.getConfOutputCharset());
        registerPostSFTP(confOutputFilename, outputConfig.getConfSftpOutput(), outputConfig.getConfSftp());

        return writer;
    }

    @Override
    protected boolean closeWriter(OutputConfig outputConfig, DataTable dataTable, Writer writer, boolean success) {
        boolean ancestorSuccess = super.closeWriter(outputConfig, dataTable, writer, success);

        if (ancestorSuccess && success) {
            return generateConversionFile();
        }

        return false;
    }

    private boolean generateConversionFile() {
        return false;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConfOutput.class);
    }
}
