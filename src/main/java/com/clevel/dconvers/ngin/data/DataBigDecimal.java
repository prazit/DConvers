package com.clevel.dconvers.ngin.data;

import com.clevel.dconvers.Application;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DataBigDecimal extends DataColumn {

    private BigDecimal value;

    public DataBigDecimal(Application application, int index, int type, String name, BigDecimal value) {
        super(application, index, type, name);

        this.value = value;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DataBigDecimal.class);
    }

    @Override
    public DataColumn clone(String value) {
        BigDecimal bigDecimal;

        try {
            bigDecimal = BigDecimal.valueOf(Double.parseDouble(value));
        } catch (Exception ex) {
            bigDecimal = null;
        }

        DataBigDecimal dataBigDecimal = new DataBigDecimal(application, index, type, name, bigDecimal);
        dataBigDecimal.setNullString(nullString);
        return dataBigDecimal;
    }

    @Override
    public DataColumn clone(int index, String name) {
        DataBigDecimal dataBigDecimal = new DataBigDecimal(application, index, type, name, value);
        dataBigDecimal.setNullString(nullString);
        return dataBigDecimal;
    }

    public BigDecimal getBigDecimalValue() {
        return value;
    }

    @Override
    public String getQuotedValue() {
        if (value == null) {
            return nullString;
        }

        return value.toString();
    }

    @Override
    public String getValue() {
        if (value == null) {
            return nullString;
        }

        return value.toString();
    }

    @Override
    public String getFormattedValue(String pattern) {
        if (value == null) {
            return nullString;
        }

        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(value);
    }

    @Override
    public void setValue(String value) {
        this.value = BigDecimal.valueOf(Double.parseDouble(value));
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("valid", valid)
                .append("index", index)
                .append("name", name)
                .append("type", type)
                .append("value", value)
                .toString();
    }
}
