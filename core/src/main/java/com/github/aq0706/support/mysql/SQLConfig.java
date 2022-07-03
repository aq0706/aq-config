package com.github.aq0706.support.mysql;

import com.github.aq0706.config.AqConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author lidq
 */
public class SQLConfig {
    private static SQLConfig DEFAULT = new SQLConfig();

    static {
        Properties properties = new Properties();
        InputStream propertiesStream = AqConfig.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(propertiesStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DEFAULT.jdbcUrl = properties.getProperty("mysql.url");
        DEFAULT.username = properties.getProperty("mysql.username");
        DEFAULT.password = properties.getProperty("mysql.password");
        DEFAULT.driverClassName = "com.mysql.cj.jdbc.Driver";
        DEFAULT.corePoolSize = 10;
        DEFAULT.maxPoolSize = 20;
    }

    public int maxPoolSize;
    public int corePoolSize;
    public int idleTimeout;

    public String driverClassName;
    public String username;
    public String password;
    public String jdbcUrl;
    public boolean isAutoCommit;
    public boolean isReadOnly;

    public static SQLConfig getDefault() {
        return DEFAULT;
    }
}
