package com.clevel.dconvers.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.DataFormatter;
import com.clevel.dconvers.format.SQLStatementFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DBExecuteOutput extends Output {

    public DBExecuteOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = new ArrayList<>();
        String column = outputConfig.getDbExecuteColumn();
        String dataSourceName = outputConfig.getDbExecuteDataSource();
        List<String> preSQL = outputConfig.getDbExecutePreSQL();
        List<String> postSQL = outputConfig.getDbExecutePostSQL();

        dataFormatterList.add(new SQLStatementFormatter(application, name, column, dataSourceName, preSQL, postSQL));

        return dataFormatterList;
    }

    @Override
    protected Writer openWriter(OutputConfig outputConfig, DataTable dataTable) {
        String outputPath;
        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;
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

        String outputFilename = outputPath + outputConfig.getDbExecuteOutput();
        Writer writer = createFile(outputFilename, true, false, "UTF-8");

        return writer;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DBExecuteOutput.class);
    }

}
