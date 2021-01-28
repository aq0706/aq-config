package com.github.aq0706.support.mysql.pool;

import com.github.aq0706.support.pool.ConcurrentPool;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author lidq
 */
public class SQLConnectionEntry implements ConcurrentPool.IEntry {
    private static final AtomicIntegerFieldUpdater<SQLConnectionEntry> stateUpdater;

    Connection connection;
    long lastAccessed;

    private volatile int state = 0;
    private volatile boolean evict;

    private final List<Statement> openStatements;
    private final SQLConnectionPool connectionPool;

    private final boolean isReadOnly;
    private final boolean isAutoCommit;

    static {
        stateUpdater = AtomicIntegerFieldUpdater.newUpdater(SQLConnectionEntry.class, "state");
    }

    public SQLConnectionEntry(final Connection connection, final SQLConnectionPool connectionPool, final boolean isReadOnly, boolean isAutoCommit) {
        this.connection = connection;
        this.connectionPool = connectionPool;
        this.isReadOnly = isReadOnly;
        this.isAutoCommit = isAutoCommit;
        this.lastAccessed = System.currentTimeMillis();
        this.openStatements = new ArrayList<>(16);
    }

    void recycle(final long lastAccessed) {
        if (connection != null) {
            this.lastAccessed = lastAccessed;
            this.connectionPool.returning(this);
        }
    }

    @Override
    public boolean compareAndSet(final int expectState, final int newState) {
        return stateUpdater.compareAndSet(this, expectState, newState);
    }

    @Override
    public void setState(final int newState) {
        state = newState;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public long getLastAccessed() {
        return lastAccessed;
    }
}
