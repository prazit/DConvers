package com.clevel.dconvers;

import com.clevel.dconvers.conf.DataConversionConfigFile;
import com.clevel.dconvers.conf.Defaults;
import org.junit.Test;

public class SaveConfigUT {

    @Test
    public void saveConfigs() {

        String sourceFileName = "/Apps/DConvers/conf/dataconversion" + Defaults.CONFIG_FILE_EXT.getStringValue();
        DConvers dconvers = new DConvers(sourceFileName);
        dconvers.setManualMode(true);
        DataConversionConfigFile dataConversionConfigFile = dconvers.dataConversionConfigFile;



    }


}
