package com.clevel.dconvers.ngin.transform;

public enum TransformTypes {

    COMPILE(CompileTransform.class),
    CONCAT(ConcatTransform.class),
    FIXEDLENGTH(FixedLengthTransform.class),
    FORMAT(FormatTransform.class),
    GET(GetTransform.class),
    REMOVE(RemoveTransform.class),
    ROWCOUNT(RowCountTransform.class),
    SUM(SumTransform.class);

    private Class transformClass;

    TransformTypes(Class transformClass) {
        this.transformClass = transformClass;
    }

    public Class getTransformClass() {
        return transformClass;
    }

}
