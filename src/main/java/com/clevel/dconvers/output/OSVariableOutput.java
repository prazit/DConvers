package com.clevel.dconvers.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.DataFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.List;
import java.util.Map;

public class OSVariableOutput extends Output {

    public OSVariableOutput(Application application, String name) {
        super(application, name);
    }

    @Override
    protected List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable) {
        return null;
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
    public String print(OutputConfig outputConfig, DataTable dataTable) {
        String variableColumnName = outputConfig.getOsVariableName();
        String valueColumnName = outputConfig.getOsVariableValue();

        Writer writer;
        try {
            writer = openWriter(outputConfig, dataTable);
        } catch (Exception ex) {
            error("Print failed", ex);
            return null;
        }
        if (writer == null) {
            return null;
        }

        boolean printSuccess = true;
        try {
            /* loop all var in dataTable and then set into ordinary map of environment */
            Map<String, String> env = System.getenv();
            String varName;
            String varValue;
            for (DataRow dataRow : dataTable.getRowList()) {
                varName = dataRow.getColumn(variableColumnName).getValue();
                varValue = dataRow.getColumn(valueColumnName).getValue();

                writer.write("setenv " + varName + "=" + varValue + "\n");
                env.put(varName, varValue);
            }
        } catch (Exception ex) {
            error("print failed!", ex);
            return null;
        }

        if (!closeWriter(outputConfig, dataTable, writer, printSuccess) /*|| !printSuccess*/) {
            return null;
        }
        return outputName;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(OSVariableOutput.class);
    }

}
