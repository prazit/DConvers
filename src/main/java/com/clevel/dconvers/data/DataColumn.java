package com.clevel.dconvers.data;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.AppBase;

public abstract class DataColumn extends AppBase {

    protected int index;
    protected int type;
    protected String quotes;
    protected String nullString;

    DataColumn(Application application, int index, int type, String name) {
        super(application, name);

        this.index = index;
        this.type = type;

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
