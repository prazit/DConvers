package com.clevel.dconvers;

import me.tongfei.progressbar.ProgressBarStyle;

import java.io.PrintStream;

public class CLIProgressBar implements ProgressBar {

    me.tongfei.progressbar.ProgressBar progressBar;

    public CLIProgressBar(String task, long initialMax) {
        progressBar = new me.tongfei.progressbar.ProgressBar(task, initialMax);
    }

    public CLIProgressBar(String task, long initialMax, ProgressBarStyle style) {
        progressBar = new me.tongfei.progressbar.ProgressBar(task, initialMax, style);
    }

    public CLIProgressBar(String task, long initialMax, int updateIntervalMillis) {
        progressBar = new me.tongfei.progressbar.ProgressBar(task, initialMax, updateIntervalMillis);
    }

    public CLIProgressBar(String task, long initialMax, int updateIntervalMillis, PrintStream os, ProgressBarStyle style, String unitName, long unitSize) {
        progressBar = new me.tongfei.progressbar.ProgressBar(task, initialMax, updateIntervalMillis, os, style, unitName, unitSize);
    }

    @Override
    public ProgressBar stepTo(long n) {
        progressBar.stepTo(n);
        return this;
    }

    @Override
    public ProgressBar step() {
        progressBar.step();
        return this;
    }

    @Override
    public ProgressBar maxHint(long n) {
        progressBar.maxHint(n);
        return this;
    }

    @Override
    public void close() {
        progressBar.close();
    }
}
