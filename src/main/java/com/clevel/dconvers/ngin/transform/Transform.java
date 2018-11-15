package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.AppBase;
import com.clevel.dconvers.ngin.data.DataTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class Transform extends AppBase {

    protected Map<String, String> argumentList;

    public void setArgumentList(Map<String, String> argumentList) {
        this.argumentList = argumentList;
    }

    protected String getArgument(String argumentName) {
        return getArgument(argumentName, "");
    }

    protected String getArgument(String argumentName, String defaultValue) {
        String value = this.argumentList.get(argumentName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public Transform(Application application, String name) {
        super(application, name);
    }

    public abstract boolean transform(DataTable dataTable);

    //------- Some shared functions here

    /**
     * all index in parameters start at 1.
     */
    protected List<Integer> createIndexList(String[] arguments, int firstArgument, int minIndex, int maxIndex) {
        List<Integer> indexList = new ArrayList<>();
        String argument;
        int index;
        int last;

        // convert index to based 0.
        minIndex--;
        maxIndex--;

        for (int argIndex = firstArgument; argIndex < arguments.length; argIndex++) {
            argument = arguments[argIndex];
            if (argument.indexOf("-") > 0) {
                String[] range = argument.split("[-]");
                index = Integer.valueOf(range[0]) - 1;
                last = Integer.valueOf(range[1]) - 1;
                if (last > maxIndex) {
                    last = maxIndex;
                }

                while (index <= last) {
                    indexList.add(index);
                    index = index + 1;
                }
            } else {
                index = Integer.valueOf(argument) - 1;
                if (index < minIndex) {
                    continue;
                } else if (index > maxIndex) {
                    break;
                }
                indexList.add(index);
            }
        }

        log.debug("Transform.createIndexList => {}", indexList);
        return indexList;
    }

    protected String getFirstValue(String commaSeparatedValues) {
        int index = commaSeparatedValues.indexOf(",");
        if (index < 0) {
            return commaSeparatedValues;
        }

        return commaSeparatedValues.substring(0, index);
    }

}
