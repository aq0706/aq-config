package com.github.aq0706.config.pool.sql;

import com.github.aq0706.config.pool.SourcePool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author lidq
 */
public class SQLConnectionPool extends SourcePool<SQLConnectionEntry> {

    private static SQLConnectionPool DEFAULT = null;

    private final SQLConnectionFactory sqlConnectionFactory;
    private final SQLConfig config;

    public SQLConnectionPool(final SQLConfig config) {
        super(config.maxPoolSize, config.corePoolSize, config.idleTimeout);

        this.config = config;
        this.sqlConnectionFactory = new SQLConnectionFactory(config);
    }

    public static SQLConnectionPool getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }

        DEFAULT = new SQLConnectionPool(SQLConfig.getDefault());
        return DEFAULT;
    }

    public Connection getConnection() throws SQLException {
        return getSource().connection;
    }

    void returning(final SQLConnectionEntry entry) {
        super.recycle(entry);
    }

    @Override
    protected SQLConnectionEntry createPoolEntry() {
        Connection connection = null;
        try {
            connection = sqlConnectionFactory.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (connection != null) {
            SQLConnectionEntry entry = new SQLConnectionEntry(connection, this, config.isReadOnly, config.isAutoCommit);
            entry.connection = createConnectionProxy(connection, entry);
            return entry;
        } else {
            return null;
        }
    }

    @Override
    protected void closePoolEntry(final SQLConnectionEntry entry, final String closureReason) {
        if (entry.connection != null) {
            try {
                entry.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String poolName() {
        return "MySQL Connection";
    }

    @Override
    protected boolean isAlive(final SQLConnectionEntry entry) {
        try {
            return entry.connection.isValid(10);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Connection createConnectionProxy(final Connection delegate, final SQLConnectionEntry entry) {
        InvocationHandler handler = (proxy, method, args) -> {
            final String methodName = method.getName();
            if ("close".equals(methodName)) {
                entry.recycle(System.currentTimeMillis());
                return Void.TYPE;
            } else if ("commit".equals(methodName)) {
                delegate.commit();
                entry.lastAccessed = System.currentTimeMillis();
            } else if ("rollback".equals(methodName)) {
                if (args.length == 0) {
                    delegate.rollback();
                } else if (args.length == 1) {
                    delegate.rollback((Savepoint)args[0]);
                }
                entry.lastAccessed = System.currentTimeMillis();
            }

            return method.invoke(delegate, args);
        };
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{ Connection.class }, handler);
    }
}
