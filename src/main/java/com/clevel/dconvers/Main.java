package com.clevel.dconvers;

import com.clevel.dconvers.ngin.TimeTracker;
import com.clevel.dconvers.ngin.TimeTrackerKey;

public class Main {

    public static void main(String[] args) {
        TimeTracker timeTracker = new TimeTracker();
        timeTracker.start(TimeTrackerKey.OVERALL, "main process");

        DConvers dconvers = new DConvers(args);
        dconvers.timeTracker = timeTracker;
        dconvers.start();
    }

}