package com.ifelse.posturometer;

/**
 * Created by Bruno on 16/11/2015.
 */
public class DelayThread extends Thread {

    private boolean mRun;
    private long mDelay;
    private boolean mPaused;
    private Object mPauseLock;

    DelayThread() {
        mRun = true;
        mDelay = 0;
        mPaused = false;
        mPauseLock = new Object();
    }

    @Override
    public void run() {
        while(mRun) {

            try {
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pauseLock();

        }
    }

    public void setPaused(boolean paused) {
        synchronized (mPauseLock) {
            if(paused) {
                mPaused = true;
            } else {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }
    }

    private void pauseLock() {
        synchronized (mPauseLock) {
            while (mPaused) {
                try {
                    mPauseLock.wait();
                } catch (InterruptedException e) {}
            }
        }
    }

    public void setDelay(long delay) {
        mDelay = delay;
    }
}
