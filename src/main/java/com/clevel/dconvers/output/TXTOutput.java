package com.clevel.dconvers.output;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.DataFormatter;
import com.clevel.dconvers.format.FixedLengthFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixed Length File Format
 */
public class TXTOutput extends Output {

    public TXTOutput(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        dataFormatterList.add(new FixedLengthFormatter(dconvers, name, outputConfig));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String outputPath = getRootPath(dataTable);
        String txtOutputFilename = outputPath + outputConfig.getTxtOutput();

        Writer writer = createFile(txtOutputFilename, outputConfig.isTxtOutputAutoCreateDir(), outputConfig.isTxtOutputAppend(), outputConfig.getTxtOutputCharset());
        registerPostSFTP(txtOutputFilename, outputConfig.getTxtSftpOutput(), outputConfig.getTxtSftp());

        return writer;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(TXTOutput.class);
    }

}
