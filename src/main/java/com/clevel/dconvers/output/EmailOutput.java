package com.clevel.dconvers.output;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.DataFormatter;
import com.clevel.dconvers.format.HtmlEmailFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class EmailOutput extends Output {

    public EmailOutput(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();

        HtmlEmailFormatter htmlEmailFormatter = new HtmlEmailFormatter(dconvers, name, outputConfig);
        dataFormatterList.add(htmlEmailFormatter);

        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String outputPath;
        DataConversionConfigFile dataConversionConfigFile = dconvers.dataConversionConfigFile;
        switch (dataTable.getTableType()) {
            case SRC:
                outputPath = dataConversionConfigFile.getOutputSourcePath();
                break;

            case TAR:
                outputPath = dataConversionConfigFile.getOutputTargetPath();
                break;

            default:
                outputPath = dataConversionConfigFile.getOutputMappingPath();
        }

        String outputFilename = outputPath + outputConfig.getEmailOutput();
        Writer writer = createFile(outputFilename, true, false, "UTF-8");

        return writer;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(EmailOutput.class);
    }

}
