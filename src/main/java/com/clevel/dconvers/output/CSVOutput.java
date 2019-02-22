package com.clevel.dconvers.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.CSVFormatter;
import com.clevel.dconvers.format.DataFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Comma Separated Values
 */
public class CSVOutput extends Output {

    public CSVOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        dataFormatterList.add(new CSVFormatter(application, name, outputConfig));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String outputPath = getRootPath(dataTable);
        String csvOutputFilename = outputPath + outputConfig.getCsvOutput();

        Writer writer = createFile(csvOutputFilename, outputConfig.isCsvOutputAutoCreateDir(), outputConfig.isCsvOutputAppend(), outputConfig.getCsvOutputCharset());
        registerPostSFTP(csvOutputFilename, outputConfig.getCsvSftpOutput(), outputConfig.getCsvSftp());

        return writer;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(CSVOutput.class);
    }

}
