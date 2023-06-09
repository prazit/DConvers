package com.clevel.dconvers.dynvalue;

import com.clevel.dconvers.DConvers;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;

public class OperatorSubtract extends Operator {

    public OperatorSubtract(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    public String compute(String leftOperand, String rightOperand) {
        if (leftOperand.indexOf("[.]") > 0 || rightOperand.indexOf("[.]") > 0) {
            return computeBigDecimal(leftOperand, rightOperand);
        }
        return computeLong(leftOperand, rightOperand);
    }

    private String computeLong(String leftOperand, String rightOperand) {
        if (!NumberUtils.isCreatable(leftOperand) || !NumberUtils.isCreatable(rightOperand)) {
            return null;
        }
        
        long left = NumberUtils.createLong(leftOperand);
        long right = NumberUtils.createLong(rightOperand);
        return String.valueOf(left - right);
    }

    private String computeBigDecimal(String leftOperand, String rightOperand) {
        if (!NumberUtils.isCreatable(leftOperand) || !NumberUtils.isCreatable(rightOperand)) {
            return null;
        }

        BigDecimal left = NumberUtils.createBigDecimal(leftOperand);
        BigDecimal right = NumberUtils.createBigDecimal(rightOperand);
        return left.subtract(right).toString();
    }

}
