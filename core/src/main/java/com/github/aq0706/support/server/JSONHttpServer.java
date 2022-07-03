package com.github.aq0706.support.server;

import com.github.aq0706.support.json.JSON;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JSONHttpServer {

    private static final ThreadPoolExecutor httpExecutors = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(256),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", (HttpExchange httpExchange) -> {
            httpExecutors.submit(() -> {
                try {
                    DispatchResult result = Dispatcher.handle(httpExchange);
                    byte[] response = JSON.toJSONString(result.result).getBytes();

                    httpExchange.getResponseHeaders().add("Content-Type", "application/json");
                    httpExchange.sendResponseHeaders(result.httpStatusCode, response.length);
                    httpExchange.getResponseBody().write(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        httpExchange.sendResponseHeaders(500, 0);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                try {
                    httpExchange.getResponseBody().flush();
                    httpExchange.getResponseBody().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        server.start();

        System.out.println("HttpServer started on port " + port);
    }
}
