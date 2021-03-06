package com.github.aq0706.support.server;

import com.github.aq0706.support.json.JSON;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class JSONHttpServer {

    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", (HttpExchange httpExchange) -> {
            try {
                DispatchResult result = Dispatcher.handle(httpExchange);
                byte[] response = JSON.toJSONString(result.result).getBytes();

                httpExchange.getResponseHeaders().add("Content-Type", "application/json");
                httpExchange.sendResponseHeaders(result.httpStatusCode, response.length);
                httpExchange.getResponseBody().write(response);
            } catch (Exception e) {
                e.printStackTrace();
                httpExchange.sendResponseHeaders(500, 0);
            }

            httpExchange.getResponseBody().flush();
            httpExchange.getResponseBody().close();
        });
        server.start();
    }
}
