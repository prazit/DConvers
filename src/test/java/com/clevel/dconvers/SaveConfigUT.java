package com.clevel.dconvers;

import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.transform.TransformTypes;
import com.clevel.dconvers.ngin.Pair;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class SaveConfigUT {

    Logger log = LoggerFactory.getLogger(SaveConfigUT.class);
    DConvers dconvers;
    String configPath = "/Apps/DConvers/conf/";
    ConfigFileTypes configFileTypes;

    @Test
    public void manualConfigs() {
        log.debug("---- saveConfigs begin ----");

        configFileTypes = ConfigFileTypes.PROPERTIES;
        String fileName = configPath + "dataconversion" + getFileExt(configFileTypes);
        createEmptyFile(fileName);

        dconvers = new DConvers(new String[]{
                "--library-mode=manual"
                , "--source-type=" + configFileTypes.name()
                , "--source=" + fileName
                /*,"--save-default-value"*/
        });

        DataConversionConfigFile dataConversionConfigFile = dconvers.dataConversionConfigFile;
        initDataConversionConfigFile(dataConversionConfigFile);

        try {
            log.info("dataConversionConfigFile.saveProperties...");
            dataConversionConfigFile.saveProperties();
            log.info("Save properties success.\n");

            log.info("Outputs:");
            log.info("+ {}", fileName);
            for (ConverterConfigFile file : dataConversionConfigFile.getConverterConfigMap().values()) {
                log.info("+ {}", file.getName());
            }
        } catch (Exception ex) {
            log.error("dataConversionConfigFile.saveProperties error: ", ex);
        }

        log.debug("---- saveConfigs end ----");
    }

    private String getFileExt(ConfigFileTypes configFileTypes) {
        switch (configFileTypes) {
            case PROPERTIES:
                return ".conf";
            case XML:
                return ".xml";
            case JSON:
                return ".json";
        }
        return ".txt";
    }

    private void initDataConversionConfigFile(DataConversionConfigFile dataConversionConfigFile) {
    /*dataConversionConfigFile.setPluginsCalcList();
    dataConversionConfigFile.setPluginsDataSourceList();
    dataConversionConfigFile.setPluginsOutputList();*/

        dataConversionConfigFile.setExitOnError(true);
        /*dataConversionConfigFile.setErrorCode(-1);
        dataConversionConfigFile.setSuccessCode(0);
        dataConversionConfigFile.setWarningCode(1);*/

        /*dataConversionConfigFile.setOutputSourcePath("");
        dataConversionConfigFile.setOutputMappingPath("");
        dataConversionConfigFile.setOutputTargetPath("");*/

        dataConversionConfigFile.setSourceFileNumber(1);
        dataConversionConfigFile.setMappingFileNumber(51);
        dataConversionConfigFile.setTargetFileNumber(101);

        dataConversionConfigFile.setDataSourceConfigMap(getDataSourceConfigMap());
        dataConversionConfigFile.setSftpConfigMap(getSftpConfigMap());
        dataConversionConfigFile.setSmtpConfigMap(getSmtpConfigMap());

        dataConversionConfigFile.setConverterConfigMap(getConverterConfigMap());
    }

    private void createEmptyFile(String fileName) {
        try {
            File file = new File(fileName);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) parentFile.mkdirs();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("\n");
            fileWriter.close();
        } catch (IOException ex) {
            log.error("createEmptyFile(" + fileName + ") failed, ", ex);
        }
    }

    private HashMap<String, ConverterConfigFile> getConverterConfigMap() {
        HashMap<String, ConverterConfigFile> converterMap = new HashMap<>();

        ConverterConfigFile converterConfigFile = getConverterConfigFile("firstconverter");
        converterMap.put(converterConfigFile.getName().toUpperCase(), converterConfigFile);

        converterConfigFile = getConverterConfigFile("secondconverter");
        converterMap.put(converterConfigFile.getName().toUpperCase(), converterConfigFile);

        return converterMap;
    }

    int converterCount = 0;

    private ConverterConfigFile getConverterConfigFile(String name) {
        String fileName = configPath + name + getFileExt(configFileTypes);
        createEmptyFile(fileName);
        ConverterConfigFile converterConfigFile = new ConverterConfigFile(dconvers, fileName);

        converterConfigFile.setIndex(++converterCount);

        HashMap<String, SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();

        SourceConfig sourceConfig = getSourceConfig("firstdatatable", converterConfigFile.getProperties());
        sourceConfigMap.put(sourceConfig.getName().toUpperCase(), sourceConfig);

        sourceConfig = getSourceConfig("seconddatatable", converterConfigFile.getProperties());
        sourceConfigMap.put(sourceConfig.getName().toUpperCase(), sourceConfig);

        HashMap<String, TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();

        TargetConfig targetConfig = getTargetConfig("firsttransformtable", converterConfigFile.getProperties());
        targetConfigMap.put(targetConfig.getName().toUpperCase(), targetConfig);

        return converterConfigFile;
    }

    int targetCount = 0;

    @SuppressWarnings("unchecked")
    private TargetConfig getTargetConfig(String name, Configuration converterConfigFile) {
        TargetConfig targetConfig = new TargetConfig(dconvers, name, converterConfigFile);

        targetConfig.setSource("firstdatatable");
        targetConfig.getSourceList().add(targetConfig.getSource());
        targetConfig.setIndex(++targetCount);
        targetConfig.setId("column_A");

        List<Pair<String, String>> columnList = targetConfig.getColumnList();
        columnList.add(new Pair<>("column_A", "column_1"));
        columnList.add(new Pair<>("column_B", "column_2"));

        List<Pair<TransformTypes, HashMap<String, String>>> transformList = targetConfig.getTransformConfig().getTransformList();

        transformList.add(new Pair(TransformTypes.ROWCOUNT, getParameterMap("arguments", "SRC:firstdatatable")));
        transformList.add(new Pair(TransformTypes.ROWFILTER, getParameterMap("arguments", "exclude,function_name=null")));
        transformList.add(new Pair(TransformTypes.CONCAT, getParameterMap("arguments", "replace:branch_id_function_name,branch_id,underscore,function_name")));
        transformList.add(new Pair(TransformTypes.FIXEDLENGTH, getParameterMap(
                "arguments", "FORMATTED:4,STR:1,STR:8,STR:6",
                "format.date", "ddMMyyyy",
                "format.datetime", "ddMMyyyyHHmmss"
        )));


        OutputConfig outputConfig = targetConfig.getOutputConfig();
        enableOutputs(outputConfig);

        outputConfig = targetConfig.getMappingOutputConfig();
        enableOutputs(outputConfig);

        outputConfig = targetConfig.getTransferOutputConfig();
        enableOutputs(outputConfig);

        return targetConfig;
    }

    private HashMap<String, String> getParameterMap(String... argument) {
        HashMap<String, String> parameterMap = new HashMap<>();
        int size = argument.length;
        for (int index = 0; index < size; index += 2) {
            parameterMap.put(argument[index], argument[index + 1]);
        }
        return parameterMap;
    }

    private void enableOutputs(OutputConfig outputConfig) {
        outputConfig.setSrc(true);
        outputConfig.setTar(true);
        outputConfig.setSql(true);
        outputConfig.setMarkdown(true);
        outputConfig.setPdf(true);
        outputConfig.setTxt(true);
        outputConfig.setCsv(true);
        outputConfig.setDbInsert(true);
        outputConfig.setDbUpdate(true);
        outputConfig.setDbExecute(true);
        outputConfig.setOsVariable(true);
        outputConfig.setEmail(true);
    }

    int sourceCount = 0;

    private SourceConfig getSourceConfig(String name, Configuration converterConfigFile) {
        SourceConfig sourceConfig = new SourceConfig(dconvers, name, converterConfigFile);

        sourceConfig.setIndex(++sourceCount);
        sourceConfig.setDataSource("tradefinance");
        sourceConfig.setId("column_1");
        sourceConfig.setQuery("select 1 as column_1, 2 as column_2 from dual");
        sourceConfig.setTarget(true);

        OutputConfig outputConfig = sourceConfig.getOutputConfig();
        enableOutputs(outputConfig);

        return sourceConfig;
    }

    private HashMap<String, HostConfig> getSmtpConfigMap() {
        HashMap<String, HostConfig> smtpConfigMap = new HashMap<>();
        return smtpConfigMap;
    }

    private HashMap<String, HostConfig> getSftpConfigMap() {
        HashMap<String, HostConfig> sftpConfigMap = new HashMap<>();

        HostConfig hostConfig = getHostConfig("uatftp101");
        sftpConfigMap.put(hostConfig.getName().toUpperCase(), hostConfig);

        hostConfig = getHostConfig("uatftp102");
        sftpConfigMap.put(hostConfig.getName().toUpperCase(), hostConfig);

        return sftpConfigMap;
    }

    private HostConfig getHostConfig(String name) {
        HostConfig hostConfig = new HostConfig(dconvers, name, Property.SFTP);
        hostConfig.setHost("192.168.0.1");
        hostConfig.setPort(21);
        hostConfig.setUser("user");
        hostConfig.setPassword("password");
        hostConfig.setUserEncrypted(false);
        hostConfig.setPasswordEncrypted(false);
        hostConfig.setRetry(3);
        hostConfig.setTmp("downloaded/");
        return hostConfig;
    }

    private HashMap<String, DataSourceConfig> getDataSourceConfigMap() {
        HashMap<String, DataSourceConfig> dataSourceConfigHashMap = new HashMap<>();

        DataSourceConfig dataSourceConfig = getDataSourceConfig("tradefinance");
        dataSourceConfigHashMap.put(dataSourceConfig.getName().toUpperCase(), dataSourceConfig);

        dataSourceConfig = getDataSourceConfig("treasury");
        dataSourceConfigHashMap.put(dataSourceConfig.getName().toUpperCase(), dataSourceConfig);

        return dataSourceConfigHashMap;
    }

    private DataSourceConfig getDataSourceConfig(String name) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig(dconvers, name);

        dataSourceConfig.setUrl("jdbc:oracle:thin:@172.20.8.67:1521:FCUAT2");
        dataSourceConfig.setDriver("oracle.jdbc.driver.OracleDriver");
        dataSourceConfig.setSchema("account");
        dataSourceConfig.setUser("password");
        dataSourceConfig.setPassword("password");
        dataSourceConfig.setRetry(3);

        /*-- for EmailDataSource
        dataSourceConfig.setSsl(false);
        dataSourceConfig.setHost("localhost:5210");*/

        /*dataSourceConfig.setValueQuotes("'");
        dataSourceConfig.setNameQuotes("'");*/

        /*dataSourceConfig.setUserEncrypted(false);
        dataSourceConfig.setPasswordEncrypted(false);*/

        /*List<Pair<String, String>> propList = new ArrayList<>();
        propList.add(new Pair<>("autoCommit", "false"));
        dataSourceConfig.setPropList(propList);*/

        /*dataSourceConfig.setPre("set system=1;");
        dataSourceConfig.setPost("commit;set system=0;");*/

        return dataSourceConfig;
    }


}
