package com.github.aq0706.config.pool;

import java.sql.Connection;

/**
 * @author lidq
 */
public class SQLConnectionPool extends SourcePool {

    private final SQLConnectionFactory sqlConnectionFactory;

    public SQLConnectionPool(SQLConfig config) {
        super(config);

        this.sqlConnectionFactory = new SQLConnectionFactory(config);
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
}
