package com.github.aq0706.support.pool;

import java.util.concurrent.Semaphore;

/**
 * @author lidq
 */
public class SuspendResumeLock {

    private static final int MAX_PERMITS = 10000;
    private final Semaphore acquisitionSemaphore;

    SuspendResumeLock() {
        acquisitionSemaphore = new Semaphore(MAX_PERMITS, true);
    }

    void acquire() {
        if (acquisitionSemaphore.tryAcquire()) {
            return;
        }

        acquisitionSemaphore.acquireUninterruptibly();
    }

    void release() {
        acquisitionSemaphore.release();
    }

    public void suspend() {
        acquisitionSemaphore.acquireUninterruptibly(MAX_PERMITS);
    }

    public void resume() {
        acquisitionSemaphore.release(MAX_PERMITS);
    }
}
