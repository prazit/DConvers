package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.UtilBase;
import com.clevel.dconvers.ngin.calc.Calc;
import com.clevel.dconvers.ngin.calc.CalcFactory;
import com.clevel.dconvers.ngin.calc.CalcTypes;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Transform extends UtilBase {

    protected HashMap<String, String> argumentList;

    public void setArgumentList(HashMap<String, String> argumentList) {
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

    protected boolean calcToRowList(List<DataRow> destRowList, CalcTypes calcType, String arguments, DataTable currentTable, String newColumnName, int newColumnIndex) {
        List<DataRow> newRowList = new ArrayList<>();

        Calc calculator = CalcFactory.getCalc(application, calcType);
        calculator.setArguments(arguments);

        Converter currentConverter = application.currentConverter;
        currentConverter.setCurrentTable(currentTable);

        DataColumn value;
        DataRow newRow;
        int rowIndex = -1;

        for (DataRow row : destRowList) {
            rowIndex++;
            currentConverter.setCurrentRowIndex(rowIndex);

            value = calculator.calc();
            value.setName(newColumnName);

            newRow = insertReplaceColumn(row, newColumnName, newColumnIndex, value);
            if (newRow == null) {
                return false;
            }
            newRowList.add(newRow);
        }

        destRowList.clear();
        destRowList.addAll(newRowList);

        return true;
    }

}
