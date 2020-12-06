package com.github.aq0706.config.pool.sql;

import com.github.aq0706.config.pool.SourcePool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author lidq
 */
public class SQLConnectionPool extends SourcePool<SQLConnectionEntry> {

    private final SQLConnectionFactory sqlConnectionFactory;
    private final SQLConfig config;

    public SQLConnectionPool(SQLConfig config) {
        super(config.maxPoolSize, config.corePoolSize, config.idleTimeout);

        this.config = config;
        this.sqlConnectionFactory = new SQLConnectionFactory(config);
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
            return new SQLConnectionEntry(connection, this, config.isReadOnly, config.isAutoCommit);
        } else {
            return null;
        }
    }

    @Override
    protected void closePoolEntry(SQLConnectionEntry entry, String closureReason) {
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
    protected boolean isAlive(SQLConnectionEntry entry) {
        try {
            return entry.connection.isValid(10);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
