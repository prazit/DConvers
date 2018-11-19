package com.clevel.dconvers.ngin.transform;

public enum TransformTypes {

    FIXEDLENGTH(FixedLengthTransform.class),
    CONCAT(ConcatTransform.class),
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
