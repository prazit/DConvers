package com.clevel.dconvers;

import com.clevel.dconvers.conf.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class SaveConfigUT {

    Logger log = LoggerFactory.getLogger(SaveConfigUT.class);
    DConvers dconvers;

    @Test
    public void saveConfigs() {
        log.trace("---- saveConfigs begin ----");

        log.info("create DConvers...");
        String sourceFileName = "/Apps/DConvers/conf/dataconversion" + Defaults.CONFIG_FILE_EXT.getStringValue();
        dconvers = new DConvers(sourceFileName);
        dconvers.setManualMode(true);
        dconvers.loadLogger();

        log.info("create DataConversionConfigFile({})...", sourceFileName);
        DataConversionConfigFile dataConversionConfigFile = dconvers.dataConversionConfigFile;

        /*dataConversionConfigFile.setPluginsCalcList();
        dataConversionConfigFile.setPluginsDataSourceList();
        dataConversionConfigFile.setPluginsOutputList();*/

        dataConversionConfigFile.setExitOnError(true);
        dataConversionConfigFile.setErrorCode(-1);
        dataConversionConfigFile.setSuccessCode(0);
        dataConversionConfigFile.setWarningCode(1);

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

        try {
            log.info("dataConversionConfigFile.saveProperties...");
            dataConversionConfigFile.saveProperties();
            log.info("dataConversionConfigFile.saveProperties success: please find saved file: {}", sourceFileName);
        } catch (Exception ex) {
            log.error("dataConversionConfigFile.saveProperties error: ", ex);
        }

        log.trace("---- saveConfigs end ----");
    }

    private HashMap<String, ConverterConfigFile> getConverterConfigMap() {
        return new HashMap<>();
    }

    private HashMap<String, HostConfig> getSmtpConfigMap() {
        return new HashMap<>();
    }

    private HashMap<String, HostConfig> getSftpConfigMap() {
        return new HashMap<>();
    }

    private HashMap<String, DataSourceConfig> getDataSourceConfigMap() {
        HashMap<String, DataSourceConfig> dataSourceConfigHashMap = new HashMap<>();

        DataSourceConfig dataSourceConfig = getDataConfig();
        dataSourceConfigHashMap.put(dataSourceConfig.getName().toUpperCase(), dataSourceConfig);

        return dataSourceConfigHashMap;
    }

    private DataSourceConfig getDataConfig() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig(dconvers, "tradefinance");

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
