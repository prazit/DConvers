package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataTable;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Base class for all in this application framework.
 */
public abstract class AppBase extends ValidatorBase {

    protected Application application;
    protected Logger log;

    protected String name;

    public AppBase(Application application, String name) {
        this.application = application;
        this.log = loadLogger();
        this.name = name;
        valid = false;
    }

    public AppBase(String name) {
        this.name = name;
        valid = false;
    }

    public String getName() {
        return name;
    }

    //-- Need Implementation

    /**
     * Load your logger for this properties.
     *
     * @return Logger
     */
    protected abstract Logger loadLogger();

    private void afterError() {
        application.currentState.setValue(application.errorCode);
    }

    private void afterWarn() {
        application.currentState.setValue(application.warningCode);
    }

    private String currentConverterName() {
        if (application.currentConverter == null) {
            return "";
        }

        Converter converter = application.currentConverter;
        String name = " (converter:" + converter.getName() + ")";
        DataTable dataTable = converter.getCurrentTable();
        if (dataTable == null) {
            return name;
        }

        String tableName = dataTable.getName();
        tableName = tableName.contains(":") ? tableName : "SRC:" + tableName;

        name = " (table:" + dataTable.getName() + ")" + name;
        return name;
    }

    public void error(String format, Object arg1, Object arg2) {
        log.error(format + currentConverterName(), arg1, arg2);
        afterError();
    }

    public void error(String format, Object... arguments) {
        log.error(format + currentConverterName(), arguments);
        afterError();
    }

    public void error(String format, Object arg) {
        log.error(format + currentConverterName(), arg);
        afterError();
    }

    public void error(String msg, Throwable t) {
        log.error(msg + currentConverterName(), t);
        if (t instanceof OutOfMemoryError) {
            memoryLog();
        }
        afterError();
    }

    public void memoryLog() {
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonheap = memBean.getNonHeapMemoryUsage();

        log.debug("Max Memory = {}", runtime.maxMemory());
        log.debug("Total Memory = {}", runtime.totalMemory());
        log.debug("Free Memory = {}", runtime.freeMemory());

        log.debug("Heap Max = {}", heap.getMax());
        log.debug("Heap Init = {}", heap.getInit());
        log.debug("Heap Used = {}", heap.getUsed());
        log.debug("Heap Committed = {}", heap.getCommitted());

        log.debug("Non-heap Max = {}", nonheap.getMax());
        log.debug("Non-heap Init = {}", nonheap.getInit());
        log.debug("Non-heap Used = {}", nonheap.getUsed());
        log.debug("Non-heap Committed = {}", nonheap.getCommitted());
    }

    public void error(String msg) {
        log.error(msg + currentConverterName());
        afterError();
    }

}
