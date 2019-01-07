package com.clevel.dconvers.ngin;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copied from SELOS DROP 1.6 SOAPHANDLER.
 */
public class TimeTracker implements Serializable {

    public class Timer implements Serializable {
        private SimpleDateFormat sharedDateFormat;
        private SimpleDateFormat sharedTimeFormat;

        private TimeTrackerKey key;
        private long startTime;
        private long stopTime;
        private long totalTime;
        private String label;

        public Timer(TimeTrackerKey key, String label, SimpleDateFormat sharedDateFormat, SimpleDateFormat sharedTimeFormat) {
            this.key = key;
            this.label = label;
            stopTime = startTime = (new Date()).getTime();
            this.sharedDateFormat = sharedDateFormat;
            this.sharedTimeFormat = sharedTimeFormat;
        }

        public void stop() {
            stopTime = (new Date()).getTime();
            totalTime = stopTime - startTime;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("key", key)
                    .append("label", label)
                    .append("startTime", sharedDateFormat.format(startTime))
                    .append("stopTime", sharedDateFormat.format(stopTime))
                    .append("totalTime", sharedTimeFormat.format(totalTime))
                    .toString();
        }

    }

    private SimpleDateFormat sharedDateFormat;
    private SimpleDateFormat sharedTimeFormat;

    private HashMap<TimeTrackerKey, Timer> timers;
    private List<Timer> stopped;

    public TimeTracker() {
        sharedDateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        sharedTimeFormat = new SimpleDateFormat("mm:ss:SSS");
        timers = new HashMap<>();
        stopped = new ArrayList<>();
    }

    public void start(TimeTrackerKey key, String timerLabel) {
        timers.put(key, new Timer(key, timerLabel, sharedDateFormat, sharedTimeFormat));
    }

    public void stop(TimeTrackerKey key) {
        Timer timer = timers.remove(key);
        if (timer != null) {
            timer.stop();
            stopped.add(timer);
        }
    }

    @Override
    public String toString() {
        String toString;
        ToStringBuilder text = new ToStringBuilder(this, ToStringStyle.JSON_STYLE);
        for (Timer timer : stopped) {
            text.append("Timer", timer);
        }
        toString = "{Stopped_Timers:" + text.toString();

        if (timers.size() > 0) {
            text = new ToStringBuilder(this, ToStringStyle.JSON_STYLE);
            for (Timer timer : timers.values()) {
                text.append("Timer", timer);
            }
            toString += ",Non_Stop_Timers:" + text.toString();
        }

        return toString + "}";
    }
}
