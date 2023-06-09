package com.clevel.dconvers.ngin;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Base class for all in this dconvers framework.
 */
public abstract class AppBase extends ValidatorBase {

    protected DConvers dconvers;
    protected Logger log;

    protected String name;

    public AppBase(DConvers dconvers, String name) {
        this.dconvers = dconvers;
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

    private void afterError(String message) {
        if (dconvers == null || dconvers.currentState == null) {
            return;
        }
        dconvers.currentState.setValue(dconvers.errorCode);
        if (dconvers.currentStateMessage.getValue().isEmpty()) {
            dconvers.currentStateMessage.setValue(message);
        }
    }

    private void afterWarn(String message) {
        if (dconvers.currentState.getLongValue() == dconvers.errorCode) {
            return;
        }

        dconvers.currentState.setValue(dconvers.warningCode);
        if (dconvers.currentStateMessage.getValue().isEmpty()) {
            dconvers.currentStateMessage.setValue(message);
        }
    }

    private String currentConverterName() {
        if (dconvers == null || dconvers.currentConverter == null) {
            return "";
        }

        Converter converter = dconvers.currentConverter;
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

    public void error(String format, Object... arguments) {
        String message = format + currentConverterName();
        String completeMessage = formatMessage(message, arguments);

        Exception error = new Exception(completeMessage);
        dconvers.errorList.add(error);

        log.error(completeMessage);
        log.trace("", error);
        afterError(completeMessage);
    }

    public void error(String msg, Throwable t) {
        String message = msg + currentConverterName();

        Exception error = new Exception(message, t);
        dconvers.errorList.add(error);

        log.error(message);
        log.debug("", t);
        if (t instanceof OutOfMemoryError) {
            memoryLog();
        }
        afterError(formatMessage(message + t.getStackTrace()[0].toString()));
    }

    private String formatMessage(String format, Object... arguments) {
        return MessageFormatter.arrayFormat(format, arguments).getMessage();
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

}
