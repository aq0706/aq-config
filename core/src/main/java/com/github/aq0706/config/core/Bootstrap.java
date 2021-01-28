package com.github.aq0706.config.core;

import com.github.aq0706.support.mysql.DB;
import com.github.aq0706.support.mysql.pool.SQLConnectionPool;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
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
            SQLConnectionPool.init();
            DB.initTableInfo("com.github.aq0706.config.core");

            long effected;
//            Config config = new Config();
//            config.appName = "appName";
//            config.namespace = "namespace_v1";
//            config.key = "key";
//            config.value = "value";
//            effected = DB.model(Config.class).insert(config);

//            effected = DB.model(Config.class)
//                    .modify(new Pair<>("value", "new_value"))
//                    .where("id = 1")
//                    .execute();

//            effected = DB.model(Config.class)
//                    .count()
//                    .where()
//                    .execute();

            Collection<Config> result = DB.model(Config.class)
                    .select()
                    .where()
                    .query();

            System.out.println(result.size());

        } catch (Exception e) {
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
