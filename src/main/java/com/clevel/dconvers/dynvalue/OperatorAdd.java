package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;

public class OperatorAdd extends Operator {

    public OperatorAdd(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    public String compute(String leftOperand, String rightOperand) {
        if (leftOperand.indexOf("[.]") > 0 || rightOperand.indexOf("[.]") > 0) {
            return computeBigDecimal(leftOperand, rightOperand);
        }

        String computedValue = computeLong(leftOperand, rightOperand);
        log.debug("OperatorAdd.compute(left:{},right:{}) = (computedValue:{})", leftOperand, rightOperand, computedValue);
        return computedValue;
    }

    private String computeLong(String leftOperand, String rightOperand) {
        if (!NumberUtils.isCreatable(leftOperand) || !NumberUtils.isCreatable(rightOperand)) {
            return null;
        }

        long left = NumberUtils.createLong(leftOperand);
        long right = NumberUtils.createLong(rightOperand);
        return String.valueOf(left + right);
    }

    private String computeBigDecimal(String leftOperand, String rightOperand) {
        if (!NumberUtils.isCreatable(leftOperand) || !NumberUtils.isCreatable(rightOperand)) {
            return null;
        }

        BigDecimal left = NumberUtils.createBigDecimal(leftOperand);
        BigDecimal right = NumberUtils.createBigDecimal(rightOperand);
        return left.add(right).toString();
    }

}
