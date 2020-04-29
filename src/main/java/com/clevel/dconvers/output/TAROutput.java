package com.clevel.dconvers.output;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.ConverterTargetFormatter;
import com.clevel.dconvers.format.DataFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class TAROutput extends Output {
    public TAROutput(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        dataFormatterList.add(new ConverterTargetFormatter(dconvers, name, outputConfig));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String outputPath = getRootPath(dataTable);
        String tarOutputFilename = outputPath + outputConfig.getTarOutput();

        Writer writer = createFile(tarOutputFilename, outputConfig.isTarOutputAutoCreateDir(), outputConfig.isTarOutputAppend(), outputConfig.getTarOutputCharset());
        registerPostSFTP(tarOutputFilename, outputConfig.getTarSftpOutput(), outputConfig.getTarSftp());

        return writer;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(TAROutput.class);
    }
}
