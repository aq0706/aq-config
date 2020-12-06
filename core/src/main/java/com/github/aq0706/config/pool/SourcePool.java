package com.github.aq0706.config.pool;

import com.github.aq0706.config.util.Util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lidq
 */
public class Pool implements ConcurrentPool.StateListener {

    private final SQLConfig config;
    private final int maxConnectionSize;

    private DataSource dataSource;

    private final PoolEntryCreator POOL_ENTRY_CREATOR = new PoolEntryCreator();
    private final ThreadPoolExecutor addConnectionExecutor;

    public Pool(SQLConfig config) {
        this.config = config;
        this.maxConnectionSize = config.maxPoolSize;

        LinkedBlockingQueue<Runnable> addConnectionQueue = new LinkedBlockingQueue<>(config.maxPoolSize);
        this.addConnectionExecutor = Util.createThreadPoolExecutor(addConnectionQueue, "MySQL connection adder", null, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void addItem(int waiting) {
        boolean shouldAdd = waiting - maxConnectionSize >= 0;
        if (shouldAdd) {
            addConnectionExecutor.submit(POOL_ENTRY_CREATOR);
        }
    }

    private PoolEntry createPoolEntry() {
        return new PoolEntry(newConnection(), this, config.isReadOnly, config.isAutoCommit);
    }

    private Connection newConnection() {
        Connection connection = null;
        try {
            String username = config.username;
            String password = config.password;

        }
    }

    private final class PoolEntryCreator implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {

            return null;
        }
    }
}
