package com.github.aq0706.config.util;

import java.util.concurrent.Semaphore;

/**
 * @author lidq
 */
public class SuspendResumeLock {

    private static final int MAX_PERMITS = 10000;
    private final Semaphore acquisitionSemaphore;

    public SuspendResumeLock() {
        acquisitionSemaphore = new Semaphore(MAX_PERMITS, true);
    }

    public void acquire() {
        if (acquisitionSemaphore.tryAcquire()) {
            return;
        }

        acquisitionSemaphore.acquireUninterruptibly();
    }

    public void release() {
        acquisitionSemaphore.release();
    }

    public void suspend() {
        acquisitionSemaphore.acquireUninterruptibly(MAX_PERMITS);
    }

    public void resume() {
        acquisitionSemaphore.release(MAX_PERMITS);
    }
}
