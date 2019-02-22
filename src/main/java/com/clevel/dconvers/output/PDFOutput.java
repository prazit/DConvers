package com.clevel.dconvers.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.DataFormatter;
import com.clevel.dconvers.format.PDFTableFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class PDFOutput extends Output {

    public PDFOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();

        String rootPath = getRootPath(dataTable);
        String pdfOutputFileName = rootPath + outputConfig.getPdfOutput();

        dataFormatterList.add(new PDFTableFormatter(application, name, pdfOutputFileName, outputConfig.getPdfJRXML()));
        registerPostSFTP(pdfOutputFileName, outputConfig.getPdfSftpOutput(), outputConfig.getPdfSftp());

        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        return new StringWriter();
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(PDFOutput.class);
    }
}
