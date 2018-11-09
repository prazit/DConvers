package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.ngin.data.DataTable;
import com.clevel.dconvers.ngin.format.DataFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Output need to act as a service, no data need to stored within the output instance.
 */
public class Output extends AppBase {

    protected List<DataFormatter> dataFormatterList;    // all items in this list will be changed every call of print function, it's depends on the OutputConfig.

    public Output(Application application, String name) {
        super(application, name);

        this.dataFormatterList = new ArrayList<>();
    }

    public boolean print(OutputConfig outputConfig, DataTable dataTable) {
        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Output.class);
    }

    // TODO Now: Complete the design of Output base class

}
