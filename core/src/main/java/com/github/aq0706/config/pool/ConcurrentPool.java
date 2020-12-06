package com.github.aq0706.config.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

/**
 * @author lidq
 */
public class ConcurrentPool<T extends ConcurrentPool.IEntry> implements AutoCloseable {

    private final CopyOnWriteArrayList<T> sharedList;

    private final ThreadLocal<List<T>> threadList;
    private final StateListener listener;
    private final AtomicInteger waiters;
    private volatile boolean closed;

    private final SynchronousQueue<T> handoffQueue;

    public interface IEntry {
        int STATE_NOT_IN_USE = 0;
        int STATE_IN_USE = 1;
        int STATE_REMOVED = -1;
        int STATE_RESERVED = -2;

        boolean compareAndSet(int expectState, int newState);
        void setState(int newState);
        int getState();
    }

    public interface StateListener {
        void addItem(int waiting);
    }

    public ConcurrentPool(StateListener listener) {
        this.sharedList = new CopyOnWriteArrayList<>();
        this.threadList = ThreadLocal.withInitial(() -> new ArrayList<T>(16));
        this.listener = listener;
        this.waiters = new AtomicInteger();
        this.handoffQueue = new SynchronousQueue<>(true);
    }

    public T borrow(long timeout, TimeUnit timeUnit) throws InterruptedException {
        List<T> list = threadList.get();
        for (int i = list.size() - 1; i >= 0; i--) {
            T entry = list.remove(i);
            if (entry != null && entry.compareAndSet(IEntry.STATE_NOT_IN_USE, IEntry.STATE_IN_USE)) {
                return entry;
            }
        }

        int waiting = waiters.incrementAndGet();
        try {
            for (T entry : sharedList) {
                if (entry.compareAndSet(IEntry.STATE_NOT_IN_USE, IEntry.STATE_IN_USE)) {
                    return entry;
                }
            }

            listener.addItem(waiting);

            timeout = timeUnit.toNanos(timeout);
            do {
                long start = System.nanoTime();
                T entry = handoffQueue.poll(timeout, TimeUnit.NANOSECONDS);
                if (entry == null || entry.compareAndSet(IEntry.STATE_NOT_IN_USE, IEntry.STATE_IN_USE)) {
                    return entry;
                }

                timeout -= (System.nanoTime() - start);
            } while (timeout > 10_000);

            return null;
        } finally {
            waiters.decrementAndGet();
        }
    }

    public void requite(T entry) {
        entry.setState(IEntry.STATE_NOT_IN_USE);

        for (int i = 0; waiters.get() > 0; i++) {
            if (entry.getState() != IEntry.STATE_NOT_IN_USE || handoffQueue.offer(entry)) {
                return;
            } else if ((i & 0xff) == 0xff) {
                LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(10));
            } else {
                Thread.yield();
            }
        }

        List<T> threadLocalList = threadList.get();
        threadLocalList.add(entry);
    }

    public void add(T entry) {
        if (closed) {
            throw new IllegalStateException("ConcurrentPool has been closed, ignoring add()");
        }

        sharedList.add(entry);

        while (waiters.get() > 0 && !handoffQueue.offer(entry)) {
            Thread.yield();
        }
    }

    public boolean remove(T entry) {
        if (!entry.compareAndSet(IEntry.STATE_IN_USE, IEntry.STATE_REMOVED) && entry.compareAndSet(IEntry.STATE_RESERVED, IEntry.STATE_REMOVED) && !closed) {
            return false;
        }

        return sharedList.remove(entry);
    }

    @Override
    public void close() throws Exception {
        closed = true;
    }

    public List<T> values(int state) {
        List<T> list = sharedList.stream().filter(e -> e.getState() == state).collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<T> values() {
        return (List<T>) sharedList.clone();
    }

    public boolean reserve(T entry) {
        return entry.compareAndSet(IEntry.STATE_NOT_IN_USE, IEntry.STATE_REMOVED);
    }

    public void unreserve(T entry) {
        if (entry.compareAndSet(IEntry.STATE_RESERVED, IEntry.STATE_NOT_IN_USE)) {
            while (waiters.get() > 0 && !handoffQueue.offer(entry)) {
                Thread.yield();
            }
        }
    }

    public int getWaitingThreadCount() {
        return waiters.get();
    }

    public int size() {
        return sharedList.size();
    }


}
