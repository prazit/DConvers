package com.clevel.dconvers.ngin.transform;

import org.slf4j.LoggerFactory;

public enum TransformTypes {

    COMPILE(CompileTransform.class),
    CONCAT(ConcatTransform.class),
    FIXEDLENGTH(FixedLengthTransform.class),
    FORMAT(FormatTransform.class),
    GET(GetTransform.class),
    REMOVE(RemoveTransform.class),
    ROWCOUNT(RowCountTransform.class),
    ROWSPLIT(RowSplitTransform.class),
    ROWFILTER(RowFilterTransform.class),
    SUM(SumTransform.class);

    private Class transformClass;

    TransformTypes(Class transformClass) {
        this.transformClass = transformClass;
    }

    public Class getTransformClass() {
        return transformClass;
    }

    public static TransformTypes parse(String name) {
        TransformTypes transformTypes;

        try {
            name = name.toUpperCase();
            transformTypes = TransformTypes.valueOf(name);
        } catch (IllegalArgumentException ex) {
            transformTypes = null;
            LoggerFactory.getLogger(TransformTypes.class).error("TransformTypes.parse(name:{}) is failed!", name, ex);
        }

        return transformTypes;
    }

}
