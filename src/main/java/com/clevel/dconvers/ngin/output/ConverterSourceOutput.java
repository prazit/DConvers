package com.clevel.dconvers.ngin.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.ConverterSourceFormatter;
import com.clevel.dconvers.ngin.format.DataFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ConverterSourceOutput extends Output {
    public ConverterSourceOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        dataFormatterList.add(new ConverterSourceFormatter(application, name, outputConfig));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String outputPath = getRootPath(dataTable);
        String srcOutputFilename = outputPath + outputConfig.getSrcOutput();

        Writer writer = createFile(srcOutputFilename, outputConfig.isSrcOutputAutoCreateDir(), outputConfig.isSrcOutputAppend(), outputConfig.getSrcOutputCharset());
        registerPostSFTP(srcOutputFilename, outputConfig.getSrcSftpOutput(), outputConfig.getSrcSftp());

        return writer;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(ConverterSourceOutput.class);
    }
}
