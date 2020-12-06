package com.github.aq0706.config.pool.sql;

/**
 * @author lidq
 */
public class SQLConfig {
    public int maxPoolSize;
    public int corePoolSize;
    public int idleTimeout;

    public String driverClassName;
    public String username;
    public String password;
    public String jdbcUrl;
    public boolean isAutoCommit;
    public boolean isReadOnly;

}
