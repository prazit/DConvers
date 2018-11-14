package com.clevel.dconvers.ngin.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.ngin.Output;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.Target;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.DataFormatter;
import com.clevel.dconvers.ngin.format.FixedLengthFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixed Length File Format
 */
public class TXTOutput extends Output {

    public TXTOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        dataFormatterList.add(new FixedLengthFormatter(application, name, outputConfig));
        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;
        String outputPath;
        Object owner = dataTable.getOwner();

        if (owner instanceof Source) {
            outputPath = dataConversionConfigFile.getOutputSourcePath();
        } else if (owner instanceof Target) {
            outputPath = dataConversionConfigFile.getOutputTargetPath();
        } else {
            outputPath = dataConversionConfigFile.getOutputMappingPath();
        }

        Writer writer = createFile(outputPath + outputConfig.getTxtOutput(), outputConfig.isTxtOutputAppend(), outputConfig.getMarkdownOutputCharset());
        return writer;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(TXTOutput.class);
    }

}
