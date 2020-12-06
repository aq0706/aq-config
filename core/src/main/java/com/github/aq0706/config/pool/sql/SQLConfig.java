package com.github.aq0706.config.pool;

/**
 * @author lidq
 */
public class SQLConfig {
    int maxPoolSize;
    int corePoolSize;

    String driverClassName;
    String username;
    String password;
    String jdbcUrl;
    boolean isAutoCommit;
    boolean isReadOnly;
}
