package com.clevel.dconvers;

import com.clevel.dconvers.ngin.TimeTracker;
import com.clevel.dconvers.ngin.TimeTrackerKey;

public class Main {

    public static void main(String[] args) {
        TimeTracker timeTracker = new TimeTracker();
        timeTracker.start(TimeTrackerKey.OVERALL, "main process");

        Application application = new Application(args);
        application.timeTracker = timeTracker;
        application.start();
    }

}