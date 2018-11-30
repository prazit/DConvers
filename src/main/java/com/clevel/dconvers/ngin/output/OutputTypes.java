package com.clevel.dconvers.ngin.output;

public enum OutputTypes {

    // first  used: SCT Data Migration
    SQL_FILE(SQLOutput.class),
    MARKDOWN_FILE(MarkdownOutput.class),

    // first used: TMB ECM ENoti(internal)
    PDF_FILE(PDFOutput.class),

    // first used: LHBank ETL
    TXT_FILE(TXTOutput.class),
    CSV_FILE(CSVOutput.class),
    INSERT_DB(DBInsertOutput.class),
    UPDATE_DB(DBUpdateOutput.class),
    CONVERTER_SOURCE_FILE(ConverterSourceOutput.class),
    CONVERTER_TARGET_FILE(ConverterTargetOutput.class)
    ;

    private Class outputClass;

    OutputTypes(Class outputClass) {
        this.outputClass = outputClass;
    }

    public Class getOutputClass() {
        return outputClass;
    }

}
