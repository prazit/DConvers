package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.data.DataTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Transform extends AppBase {

    protected Map<String, String> argumentList;

    public void setArgumentList(Map<String, String> argumentList) {
        this.argumentList = argumentList;
    }

    protected String getArgument(String argumentName) {
        return this.argumentList.get(argumentName);
    }

    public Transform(Application application, String name) {
        super(application, name);
    }

    public abstract boolean transform(DataTable dataTable);

    //------- Some shared functions here

    protected List<Integer> createIndexList(String[] arguments, int firstElement) {
        List<Integer> indexList = new ArrayList<>();
        String argument;

        for (int argIndex = firstElement; argIndex < arguments.length; argIndex++) {
            argument = arguments[argIndex];
            if (argument.indexOf("-") > 0) {
                String[] range = argument.split("[-]");
                Integer first = Integer.valueOf(range[0]) - 1;
                Integer last = Integer.valueOf(range[1]) - 1;

                while (first.compareTo(last) < 0) {
                    indexList.add(first);
                    first = first + 1;
                }

                indexList.add(last);
            } else {
                indexList.add(Integer.valueOf(argument) - 1);
            }
        }

        log.debug("Transform.createIndexList => {}", indexList);
        return indexList;
    }

}
