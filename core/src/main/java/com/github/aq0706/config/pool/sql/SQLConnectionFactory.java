package com.github.aq0706.config.pool;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author lidq
 */
public class SQLConnectionFactory {

    private final SQLConfig config;
    private final Properties driverProperties;

    private Driver driver;

    public SQLConnectionFactory(SQLConfig config) {
        this.config = config;
        this.driverProperties = new Properties();
        this.driverProperties.setProperty("user", config.username);
        this.driverProperties.setProperty("password", config.password);

        try {
            initDriver();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init driver instance for jdbcUrl=" + config.jdbcUrl, e);
        }
    }

    public Connection create() {
        try {
            return driver.connect(config.jdbcUrl, driverProperties);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void initDriver() throws SQLException {
        if (driver == null) {
            if (config.driverClassName != null) {
                Class driverClass = null;
                try {
                    driverClass = Class.forName(config.driverClassName);
                } catch (ClassNotFoundException ignored) {
                }
                if (driverClass != null) {
                    try {
                        driver = (Driver) driverClass.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!driver.acceptsURL(config.jdbcUrl)) {
                        throw new RuntimeException("Driver " + config.driverClassName + " do not accept jdbcUrl=" + config.jdbcUrl);
                    }
                }
            }
        }

        if (driver == null) {
            driver = DriverManager.getDriver(config.jdbcUrl);
        }
    }
}
