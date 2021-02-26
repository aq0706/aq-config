package com.github.aq0706.support.server;

import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lidq
 */
public class Dispatcher {

    private final static Map<String, Method> handlers = new HashMap<>();

    public static void registerHandler(String uri, String method, Method handler) {
        if (handlers.containsKey(uri + "_" + method)) {
            throw new IllegalArgumentException("Uri: " + uri + " method: " + method + " already exists.");
        }
        handlers.put(uri + "_" + method, handler);
    }

    public static DispatchResult handle(HttpExchange httpExchange) throws Exception {
        DispatchResult result = new DispatchResult();

        String uri = httpExchange.getRequestURI().getPath();
        String method = httpExchange.getRequestMethod();

        Method handler = handlers.get(uri + "_" + method);
        if (handler == null) {
            result.httpStatusCode = 404;
        } else {
            int contentLength = Integer.parseInt(httpExchange.getRequestHeaders().getFirst("Content-length"));
            byte[] requestBody = new byte[contentLength];
            int read = httpExchange.getRequestBody().read(requestBody);
            if (read != contentLength) {
                throw new IllegalStateException("ContentLength: " + contentLength + " != read: " + read);
            }
            // TODO JSON序列化
            result.result = handler.invoke(null, httpExchange);
            result.httpStatusCode = 200;
        }

        return result;
    }
}
