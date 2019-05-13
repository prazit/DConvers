package com.clevel.dconvers.output;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.format.DataFormatter;
import com.clevel.dconvers.ngin.AppBase;
import com.clevel.dconvers.ngin.SFTP;
import com.clevel.dconvers.ngin.Source;
import com.clevel.dconvers.ngin.Target;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.*;
import java.util.List;

/**
 * Output need to act as a service, no data is stored within the output instance.
 */
public abstract class Output extends AppBase {

    private class PostSFTP {
        public String localFile;
        public String remoteFile;
        public String sftp;

        public PostSFTP(String localFile, String remoteFile, String sftp) {
            this.localFile = localFile;
            this.remoteFile = remoteFile;
            this.sftp = sftp;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("sftp", sftp)
                    .append("remoteFile", remoteFile)
                    .append("localFile", localFile)
                    .toString()
                    .replace('=', ':');
        }
    }

    private PostSFTP postSFTP;
    private String outputName;

    public Output(Application application, String name) {
        super(application, name);

        postSFTP = null;
        outputName = null;
    }

    public String print(OutputConfig outputConfig, DataTable dataTable) {
        List<DataFormatter> dataFormatterList = getFormatterList(outputConfig, dataTable);
        if (dataFormatterList == null) {
            return null;
        }

        Writer writer;
        try {
            writer = openWriter(outputConfig, dataTable);
        } catch (Exception ex) {
            error("Print failed: {}", ex.getMessage(), ex);
            return null;
        }
        if (writer == null) {
            return null;
        }

        boolean printSuccess = true;
        for (DataFormatter formatter : dataFormatterList) {
            printSuccess = formatter.print(dataTable, writer);
            formatter.reset();

            if (!printSuccess) {
                break;
            }
        }

        if (!closeWriter(outputConfig, dataTable, writer, printSuccess) || !printSuccess) {
            return null;
        }
        return outputName;
    }

    protected abstract List<DataFormatter> getFormatterList(OutputConfig outputConfig, DataTable dataTable);

    protected abstract Writer openWriter(OutputConfig outputConfig, DataTable dataTable);

    protected boolean closeWriter(OutputConfig outputConfig, DataTable dataTable, Writer writer, boolean success) {
        try {
            writer.close();
        } catch (IOException e) {
            // do nothing
        }

        if (postSFTP == null) {
            return true;
        }

        SFTP sftp = application.sftpMap.get(postSFTP.sftp.toUpperCase());
        if (sftp == null) {
            error("The sftp({}) is not found, please check sftp name ({}).", postSFTP.sftp, postSFTP);
            return false;
        }

        if (!sftp.copyToSFTP(postSFTP.localFile, postSFTP.remoteFile)) {
            return false;
        }

        log.info("Copied to SFTP, {}", postSFTP);
        return true;
    }

    protected Writer createFile(String outputFile, boolean autoCreateDir, boolean append, String charset) {
        log.debug("Output.createFile(outputFile:{}, autoCreateDir:{}, append:{}, charset:{})", outputFile, autoCreateDir, append, charset);
        Writer writer = tryToCreateFile(outputFile, append, charset);

        if (writer == null) {
            if (autoCreateDir && autoCreateDir(outputFile)) {
                writer = tryToCreateFile(outputFile, append, charset);
            } else {
                error("Output.createFile is failed! please check parameters(outputFile:{}, autoCreateDir:{}, append:{}, charset:{})", outputFile, autoCreateDir, append, charset);
                if (application.exitOnError) {
                    return null;
                }

                try {
                    writer = new PrintWriter(new OutputStreamWriter(System.out, charset));
                } catch (UnsupportedEncodingException e1) {
                    writer = new StringWriter();
                }
            }
        }

        if (writer != null) {
            log.info("Output file is created(append:{}), local-file:{}", append, outputFile);
            String fileName = outputFile + (append ? " (append)" : " (create/replace)");
            if (outputName == null) {
                outputName = fileName;
            } else {
                outputName += "," + fileName;
            }
        }

        return writer;
    }

    private Writer tryToCreateFile(String outputFile, boolean append, String charset) {
        Writer writer;

        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFile, append), charset);
        } catch (Exception e) {
            log.debug("try to create file({}) is failed, {}", outputFile, e.getMessage());
            return null;
        }

        return writer;
    }

    protected boolean autoCreateDir(String outputFile) {
        File file = new File(outputFile);
        File parentFile = file.getParentFile();
        log.debug("try to create directory path({})", parentFile);
        return parentFile.mkdirs();
    }

    protected void registerPostSFTP(String localFile, String remoteFile, String sftp) {
        if (localFile == null || remoteFile == null || sftp == null) {
            return;
        }

        postSFTP = new PostSFTP(localFile, remoteFile, sftp);
    }

    protected String getRootPath(DataTable dataTable) {
        DataConversionConfigFile dataConversionConfigFile = application.dataConversionConfigFile;
        Object owner = dataTable.getOwner();
        String outputPath;

        if (owner instanceof Source) {
            outputPath = dataConversionConfigFile.getOutputSourcePath();
        } else if (owner instanceof Target) {
            outputPath = dataConversionConfigFile.getOutputTargetPath();
        } else {
            outputPath = dataConversionConfigFile.getOutputMappingPath();
        }

        return outputPath;
    }

}
