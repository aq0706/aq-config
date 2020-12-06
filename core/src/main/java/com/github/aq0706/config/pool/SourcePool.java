package com.github.aq0706.config.pool;

import com.github.aq0706.config.util.SuspendResumeLock;
import com.github.aq0706.config.util.Util;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lidq
 */
public abstract class SourcePool<T extends ConcurrentPool.IEntry> {

    public static final int POOL_NORMAL = 0;
    public static final int POOL_SUSPENDED = 1;
    public static final int POOL_SHUTDOWN = 2;

    public volatile int poolState;

    private final ConcurrentPool<T> pool;
    private final int maxPoolSize;
    private final int minIdle;
    private final int idleTimeout;

    private final long HOUSEKEEPING_PERIOD_MS = TimeUnit.SECONDS.toMillis(30);

    private final PoolEntryCreator POOL_ENTRY_CREATOR = new PoolEntryCreator();
    private final LinkedBlockingQueue<Runnable> addConnectionQueue;
    private final ThreadPoolExecutor addConnectionExecutor;
    private final ThreadPoolExecutor closeConnectionExecutor;

    private final SuspendResumeLock suspendResumeLock;

    private final ScheduledExecutorService houseKeeperExecutorService;
    private ScheduledFuture<?> houseKeeperTask;

    public SourcePool(final int maxPoolSize, final int minIdle, final int idleTimeout) {
        this.maxPoolSize = maxPoolSize;
        this.minIdle = minIdle;
        this.idleTimeout = idleTimeout;

        this.suspendResumeLock = new SuspendResumeLock();

        this.houseKeeperExecutorService = initializeHouseKeepingExecutorService();

        this.pool = new ConcurrentPool<>(new ConcurrentPool.StateListener() {
            @Override
            public void addItem(int waiting) {
                boolean shouldAdd = waiting - addConnectionQueue.size() >= 0;
                if (shouldAdd) {
                    addConnectionExecutor.submit(POOL_ENTRY_CREATOR);
                }
            }
        });
        this.addConnectionQueue = new LinkedBlockingQueue<>(maxPoolSize);
        this.addConnectionExecutor = Util.createThreadPoolExecutor(addConnectionQueue, poolName() + " adder", null, new ThreadPoolExecutor.CallerRunsPolicy());
        this.closeConnectionExecutor = Util.createThreadPoolExecutor(new LinkedBlockingQueue<>(maxPoolSize), poolName() + " closer", null, new ThreadPoolExecutor.CallerRunsPolicy());

        this.houseKeeperTask = houseKeeperExecutorService.scheduleWithFixedDelay(new HouseKeeper(), 100L, HOUSEKEEPING_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    protected T getSource() throws SQLException {
        return getSource(30_000);
    }

    protected T getSource(final long hardTimeout) throws SQLException {
        suspendResumeLock.acquire();
        final long startTime = System.currentTimeMillis();

        try {
            long timeout = hardTimeout;
            do {
                T entry = pool.borrow(timeout, TimeUnit.MILLISECONDS);
                if (entry == null) {
                    break;
                } else if (!isAlive(entry)) {
                    closeSource(entry, "(connection is dead)");
                    timeout -= (System.currentTimeMillis() - startTime);
                } else {
                    return entry;
                }
            } while (timeout > 0);
            throw new SQLTransientConnectionException(poolName() + " - Connection is not available, request timed out after " + (System.currentTimeMillis() - startTime));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException(poolName() + " - Interrupted during connection acquisition", e);
        } finally {
            suspendResumeLock.release();
        }
    }

    protected void recycle(final T entry) {
        pool.requite(entry);
    }

    protected synchronized void shutdown() throws InterruptedException {
        poolState = POOL_SHUTDOWN;

        if (houseKeeperTask != null) {
            houseKeeperTask.cancel(false);
            houseKeeperTask = null;
        }

        addConnectionExecutor.shutdown();
        addConnectionExecutor.awaitTermination(10, TimeUnit.SECONDS);

        if (houseKeeperExecutorService != null) {
            houseKeeperExecutorService.shutdownNow();
        }

        pool.close();

        for (T entry : pool.values()) {
            closeSource(entry, "(connection shutdown when pool shutdown)");
        }

        closeConnectionExecutor.shutdown();
        closeConnectionExecutor.awaitTermination(10, TimeUnit.SECONDS);
    }

    protected void closeSource(final T entry, final String closureReason) {
        if (pool.remove(entry)) {
            closeConnectionExecutor.submit(() -> {
                closePoolEntry(entry, closureReason);
                if (poolState == POOL_NORMAL) {
                    fillPool();
                }
            });
        }
    }

    private synchronized void fillPool() {
        final int connectionsToAdd = Math.min(maxPoolSize - pool.size(), minIdle - getIdleCount()) - addConnectionQueue.size();
        for (int i = 0; i < connectionsToAdd; i++) {
            addConnectionExecutor.submit(POOL_ENTRY_CREATOR);
        }
    }

    protected abstract T createPoolEntry();

    protected abstract void closePoolEntry(T entry, String closureReason);

    protected abstract String poolName();

    protected abstract boolean isAlive(T entry);

    private ScheduledExecutorService initializeHouseKeepingExecutorService() {
        final Util.DefaultThreadFactory threadFactory = new Util.DefaultThreadFactory(poolName() + " houseKeeper", true);
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, threadFactory, new ThreadPoolExecutor.DiscardPolicy());
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    private int getIdleCount() {
        return pool.getCount(ConcurrentPool.IEntry.STATE_NOT_IN_USE);
    }

    private final class PoolEntryCreator implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            long sleepBackoff = 250L;
            while (poolState == POOL_NORMAL && shouldCreateAnotherConnection()) {
                T entry = createPoolEntry();
                if (entry != null) {
                    pool.add(entry);
                    return true;
                } else {
                    Thread.sleep(sleepBackoff);
                    sleepBackoff = Math.min(TimeUnit.SECONDS.toMillis(10), (long)(sleepBackoff * 1.5));
                }
            }
            return false;
        }

        private boolean shouldCreateAnotherConnection() {
            return pool.size() < maxPoolSize && (pool.getWaitingThreadCount() > 0 || getIdleCount() < minIdle);
        }
    }

    private final class HouseKeeper implements Runnable {

        private volatile long previous = System.currentTimeMillis() - HOUSEKEEPING_PERIOD_MS;

        @Override
        public void run() {
            final long now = System.currentTimeMillis();

            if (now + 128L < previous + HOUSEKEEPING_PERIOD_MS) {
                return;
            }

            previous = now;

            if (idleTimeout > 0L && minIdle < maxPoolSize) {
                final List<T> notInUse = pool.values(ConcurrentPool.IEntry.STATE_NOT_IN_USE);
                int toRemove = notInUse.size() - minIdle;
                for (T entry : notInUse) {
                    if (toRemove > 0 && (entry.getLastAccessed() - now) > idleTimeout && pool.reserve(entry)) {
                        closeSource(entry, "(connection has passed idleTimeout)");
                        toRemove--;
                    }
                }
            }

            fillPool();
        }
    }
}
