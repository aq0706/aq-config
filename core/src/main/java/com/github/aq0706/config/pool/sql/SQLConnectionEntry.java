package com.github.aq0706.config.pool;

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

    private volatile int state = 0;
    private volatile boolean evict;

    private final List<Statement> openStatements;
    private final SourcePool connectionPool;

    private final boolean isReadOnly;
    private final boolean isAutoCommit;

    static {
        stateUpdater = AtomicIntegerFieldUpdater.newUpdater(SQLConnectionEntry.class, "state");
    }

    public SQLConnectionEntry(final Connection connection, final SourcePool connectionPool, final boolean isReadOnly, boolean isAutoCommit) {
        this.connection = connection;
        this.connectionPool = connectionPool;
        this.isReadOnly = isReadOnly;
        this.isAutoCommit = isAutoCommit;
        this.openStatements = new ArrayList<>(16);
    }

    @Override
    public boolean compareAndSet(int expectState, int newState) {
        return false;
    }

    @Override
    public void setState(int newState) {

    }

    @Override
    public int getState() {
        return 0;
    }
}
