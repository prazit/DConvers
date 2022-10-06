package com.clevel.dconvers;

public class FackProgressBar implements ProgressBar {



    @Override
    public ProgressBar step() {
        return this;
    }

    @Override
    public ProgressBar stepTo(long n) {
        return this;
    }

    @Override
    public ProgressBar maxHint(long n) {
        return this;
    }

    @Override
    public void close() {
        /*nothing*/
    }
}
