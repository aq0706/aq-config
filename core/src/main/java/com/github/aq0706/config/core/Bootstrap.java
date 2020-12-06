package com.github.aq0706.config.core;

import com.github.aq0706.config.pool.sql.SQLConfig;
import com.github.aq0706.config.pool.sql.SQLConnectionPool;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author lidq
 */
public class Bootstrap {

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        InputStream propertiesStream = Bootstrap.class.getClassLoader().getResourceAsStream("config.properties");
        properties.load(propertiesStream);

        int port = 17060;
        try {
            port = Integer.parseInt(properties.getProperty("server.port"));
        } catch (Throwable ignore) {

        }

        try {
            SQLConfig sqlConfig = new SQLConfig();
            sqlConfig.jdbcUrl = properties.getProperty("mysql.url");
            sqlConfig.username = properties.getProperty("mysql.username");
            sqlConfig.password = properties.getProperty("mysql.password");
            sqlConfig.driverClassName = "com.mysql.cj.jdbc.Driver";
            sqlConfig.corePoolSize = 10;
            sqlConfig.maxPoolSize = 20;
            SQLConnectionPool sqlConnectionPool = new SQLConnectionPool(sqlConfig);
            Connection connection = sqlConnectionPool.getConnection();
            System.out.println(connection.isValid(10));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", (HttpExchange httpExchange) -> {
            String response = "{\"data\": \"hello\"}";
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            httpExchange.getResponseBody().write(response.getBytes());
            httpExchange.getResponseBody().flush();
            httpExchange.getResponseBody().close();
        });
        server.start();
    }
}
