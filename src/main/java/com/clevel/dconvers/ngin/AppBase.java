package com.clevel.dconvers.ngin;

import ch.qos.logback.core.Appender;
import com.clevel.dconvers.Application;
import com.clevel.dconvers.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private void afterError(String message) {
        application.currentState.setValue(application.errorCode);
        if (application.currentStateMessage.getValue().isEmpty()) {
            application.currentStateMessage.setValue(message);
        }
    }

    private void afterWarn(String message) {
        if (application.currentState.getLongValue() == application.errorCode) {
            return;
        }

        application.currentState.setValue(application.warningCode);
        if (application.currentStateMessage.getValue().isEmpty()) {
            application.currentStateMessage.setValue(message);
        }
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

    public void error(String format, Object... arguments) {
        String message = format + currentConverterName();
        log.error(message, arguments);
        afterError(formatMessage(message, arguments));
    }

    public void error(String msg, Throwable t) {
        String message = msg + currentConverterName();
        log.error(message, t);
        if (t instanceof OutOfMemoryError) {
            memoryLog();
        }
        afterError(formatMessage(message + t.getStackTrace()[0].toString()));
    }

    public void error(String msg) {
        String message = msg + currentConverterName();
        log.error(message);
        afterError(formatMessage(message));
    }

    private String formatMessage(String format, Object... arguments) {
        String message = format;
        for (Object argument : arguments) {
            message = message.replaceFirst("[{][}]", argument.toString());
        }
        return message;
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
