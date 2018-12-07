package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.ngin.ValidatorBase;

public abstract class DataColumn extends ValidatorBase {

    protected String name;
    protected int index;
    protected int type;
    protected String quotes;
    protected String nullString;

    DataColumn(int index, int type, String name) {
        this.index = index;
        this.type = type;
        this.name = name;
        nullString = "null";
        quotes = "\"";
        valid = (index >= 0) && (name != null);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getQuotes() {
        return quotes;
    }

    public void setQuotes(String quotes) {
        this.quotes = quotes;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNullString() {
        return nullString;
    }

    public void setNullString(String nullString) {
        this.nullString = nullString;
    }

    public abstract String getQuotedValue();

    public abstract String getValue();
    public abstract String getFormattedValue(String pattern);
    public abstract void setValue(String value);

    public abstract DataColumn clone(String value);

    public abstract DataColumn clone(int index, String name);
}
