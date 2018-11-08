package com.clevel.dconvers;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceUT {

    private Logger log = LoggerFactory.getLogger(SourceUT.class);

    @Test
    public void compileQuery() {
        log.debug("------- SourceUT.compileQuery is started");

        /*String[] args = "--source=\"sample-conversion.conf\" --verbose --level=DEBUG".split("[ ]");
        Application application = new Application(args);
        Converter converter = new Converter(application, "converter", new ConverterConfigFile(application, "converterConfig"));
        Source source = new Source(null, "test", converter, new SourceConfig(application, "sourceConfig", converter.getConverterConfigFile()));
        String query = source.getQuery("");*/

        //String query = getQuery("$(FILE:C:\\Users\\prazi\\Documents\\LHBank\\ETL\\Query\\DMS\\TFS_DARDWH.sql)");
        //String query = getQuery("\n-- this is my comment at the beginning\n $(FILE:C:\\\\Users\\\\prazi\\\\Documents\\\\LHBank\\\\ETL\\\\Query\\\\DMS\\\\TFS_DARDWH.sql) \n-- this is my comment at the end");

        //log.debug("SourceUT.compileQuery. queryAtTheEnd={}", query);

        log.debug("------- SourceUT.compileQuery. is ended");
    }

    //------- Temporary Functions

}
