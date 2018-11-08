package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.QueryVariable;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Source extends AppBase {

    private Converter converter;
    private SourceConfig sourceConfig;
    private DataSource dataSource;
    private DataTable dataTable;

    public Source(Application application, String name, Converter converter, SourceConfig sourceConfig) {
        super(application, name);

        this.converter = converter;
        this.sourceConfig = sourceConfig;

        valid = prepare();
        if (valid) valid = validate();

        log.trace("Source({}) is created", name);
    }

    private boolean prepare() {
        log.trace("Source({}).prepare.", name);
        String dataSourceName = sourceConfig.getDataSource();
        dataSource = application.dataSourceMap.get(dataSourceName);
        return true;
    }

    @Override
    public boolean validate() {
        log.trace("Source({}).validate.", name);

        if (dataSource == null) {
            log.error("datasource({}) is not found, required by Converter({})", sourceConfig.getDataSource(), converter.getName());
            application.hasWarning = true;
            return false;
        }

        return true;
    }

    public boolean buildDataTable() {
        log.trace("Source({}).buildDataTable.", name);
        dataTable = dataSource.getDataTable(sourceConfig.getName(), sourceConfig.getId(), getQuery());
        return dataTable != null;
    }

    public String getQuery() {
        log.debug("Source.getQuery");

        String returnValue = sourceConfig.getQuery();
        String compileResult;
        for (; true; returnValue = compileResult) {
            compileResult = compileQuery(returnValue);
            if (compileResult == null) {
                return returnValue;
            }
        }
    }

    private String compileQuery(String query) {
        int start = query.indexOf("$(");
        if (start < 0) {
            return null;
        }

        int end = query.indexOf(")", start);
        if (end < 0) {
            end = query.length() - 1;
        }

        String compile = query.substring(start + 2, end);
        log.debug("Source.compileQuery: compile({})", compile);

        int queryVarIndex = compile.indexOf(":");
        String queryVar = compile.substring(0, queryVarIndex);
        String queryVal = compile.substring(queryVarIndex + 1);
        log.debug("Source.compileQuery: queryVar({}) queryVal({})", queryVar, queryVal);

        QueryVariable queryVariable = QueryVariable.valueOf(queryVar);
        String replacement = "";
        switch (queryVariable) {
            case FILE:
                replacement = queryFromFile(queryVal);
                break;

            case SOURCE:

                break;

            case TARGET:
                break;

            case MAPPING:
                break;

        }

        String replaced = query.substring(0, start) + replacement + query.substring(end + 1);
        return replaced;
    }

    private String queryFromFile(String fileName) {

        BufferedReader br = null;
        String content = "";
        try {
            br = new BufferedReader(new FileReader(fileName));
            for (String line; (line = br.readLine()) != null; ) {
                content += line + "\n";
            }

        } catch (FileNotFoundException fx) {
            log.error("SQLSource.queryFromFile. file not found: {}", fx.getMessage());

        } catch (Exception ex) {
            log.error("SQLSource.queryFromFile. has exception: {}", ex);

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("close file {} is failed, {}", fileName, e);
                }
            }
        }

        return content;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(Source.class);
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public DataTable getDataTable() {
        if (valid && dataTable == null) {
            valid = buildDataTable();
        }
        return dataTable;
    }
}
