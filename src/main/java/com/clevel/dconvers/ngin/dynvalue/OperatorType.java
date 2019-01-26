package com.clevel.dconvers.ngin.dynvalue;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

public enum OperatorType {

    ADD('+', OperatorAdd.class),
    SUBTRACT('~', OperatorSubtract.class)
    ;

    private char operator;
    private Class operatorClass;

    OperatorType(char operator, Class operatorClass) {
        this.operator = operator;
        this.operatorClass = operatorClass;
    }

    public char getOperator() {
        return operator;
    }

    public Class getOperatorClass() {
        return operatorClass;
    }

    private static HashMap<Character, OperatorType> operatorMap;
    private static char[] operators;

    private static void prepareOperatorMap() {
        operatorMap = new HashMap<>();
        for (OperatorType expressionOperator : OperatorType.values()) {
            operatorMap.put(expressionOperator.getOperator(), expressionOperator);
        }
    }

    private static void prepareOperators() {
        Set<Character> operatorSet = operatorMap.keySet();
        operators = new char[operatorSet.size()];
        int index = 0;
        for (Character operator : operatorSet) {
            operators[index] = operator;
            index++;
        }
    }

    public static char[] getOperators() {
        if (operatorMap == null) {
            prepareOperatorMap();
        }

        if (operators == null) {
            prepareOperators();
        }

        return operators;
    }

    public static OperatorType parse(char operator) {
        if (operatorMap == null) {
            prepareOperatorMap();
        }

        OperatorType expressionOperator;
        try {
            expressionOperator = operatorMap.get(operator);
        } catch (Exception ex) {
            expressionOperator = null;
            LoggerFactory.getLogger(OperatorType.class).error("ExpressionOperator.parse(operator:{}) is failed!", operator, ex);
        }

        return expressionOperator;
    }

}
