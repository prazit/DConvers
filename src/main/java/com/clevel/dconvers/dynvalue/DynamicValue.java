package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ngin.AppBase;

public abstract class DynamicValue extends AppBase {

    protected String targetName;
    protected int targetColumnIndex;

    private DynamicValueType dynamicValueType;

    public DynamicValue(DConvers dconvers, String targetName, String targetColumnName, Integer targetColumnIndex) {
        super(dconvers, targetColumnName);
        this.targetName = targetName;
        this.targetColumnIndex = targetColumnIndex;
        valid = true;
    }

    public abstract void prepare(String sourceName, String sourceColumnName, DynamicValueType sourceColumnType, String sourceColumnArg);

    public abstract DataColumn getValue(DataRow sourceRow);

    public DynamicValueType getDynamicValueType() {
        return dynamicValueType;
    }

    public void setDynamicValueType(DynamicValueType dynamicValueType) {
        this.dynamicValueType = dynamicValueType;
    }
}
