package com.github.aq0706.config;

import com.github.aq0706.support.mysql.DB;
import com.github.aq0706.support.mysql.pool.SQLConnectionPool;
import com.github.aq0706.support.server.Dispatcher;
import com.github.aq0706.support.server.JSONHttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author lidq
 */
public class AqConfig {

    public static void main(String[] args) {
        Properties properties = new Properties();
        InputStream propertiesStream = AqConfig.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(propertiesStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int port = 17060;
        try {
            port = Integer.parseInt(properties.getProperty("server.port"));
        } catch (Throwable ignore) {

        }

        try {
            SQLConnectionPool.init();
            DB.initTableInfo("com.github.aq0706.config.core");

            Dispatcher.registerHandler("com.github.aq0706.config.core");

            JSONHttpServer.start(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
