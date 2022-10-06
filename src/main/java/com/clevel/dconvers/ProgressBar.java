package com.clevel.dconvers;

public interface ProgressBar {

    public ProgressBar step();
    public ProgressBar stepTo(long n);
    public ProgressBar maxHint(long n);
    public void close();

}
