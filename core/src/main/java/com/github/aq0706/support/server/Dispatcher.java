package com.github.aq0706.support.server;

import com.github.aq0706.lang.ReflectionUtil;
import com.github.aq0706.support.json.JSON;
import com.sun.net.httpserver.HttpExchange;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lidq
 */
public class Dispatcher {

    private final static Map<String, Handler> handlers = new HashMap<>();

    public static void registerHandler(String packageName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Collection<Class> classes = ReflectionUtil.findClassByPackage(packageName);
        for (Class beanClass : classes) {
            Annotation controllerAnnotation = beanClass.getDeclaredAnnotation(Controller.class);
            if (controllerAnnotation != null) {
                String firstPath = "";
                RequestMapping classRequestMapping = (RequestMapping) beanClass.getDeclaredAnnotation(RequestMapping.class);
                if (classRequestMapping != null) {
                    firstPath = classRequestMapping.path();
                }

                Object controller = beanClass.newInstance();
                for (Method method : beanClass.getDeclaredMethods()) {
                    RequestMapping methodRequestMapping = method.getDeclaredAnnotation(RequestMapping.class);
                    if (methodRequestMapping != null) {
                        String uri = firstPath + methodRequestMapping.path();
                        registerHandler(controller, uri, methodRequestMapping.method(), method);
                    }
                }
            }
        }
    }

    public static void registerHandler(Object controller, String uri, String method, Method handler) {
        if (handlers.containsKey(uri + "_" + method)) {
            throw new IllegalArgumentException("Uri: " + uri + " method: " + method + " already exists.");
        }
        handlers.put(uri + "_" + method, new Handler(controller, handler));
    }

    @SuppressWarnings("unchecked")
    public static DispatchResult handle(HttpExchange httpExchange) throws Exception {
        DispatchResult result = new DispatchResult();

        String uri = httpExchange.getRequestURI().getPath();
        String requestMethod = httpExchange.getRequestMethod();

        Handler handler = handlers.get(uri + "_" + requestMethod);
        if (handler == null) {
            result.httpStatusCode = 404;
        } else {
            int contentLength = Integer.parseInt(httpExchange.getRequestHeaders().getFirst("Content-length"));
            byte[] requestBody = new byte[contentLength];
            int read = httpExchange.getRequestBody().read(requestBody);
            if (contentLength != 0 && read != contentLength) {
                throw new IllegalStateException("ContentLength: " + contentLength + " != read: " + read);
            }

            Object controller = handler.controller;
            Method handlerMethod = handler.method;
            Type[] parameterTypes = handlerMethod.getGenericParameterTypes();
            if (parameterTypes.length == 0) {
                result.result = handlerMethod.invoke(controller);
            } else {
                // TODO Only support first param(JSON) for now.
                Object[] values = new Object[1];
                for (int i = 0; i < values.length; i++) {
                    values[i] = JSON.parse(requestBody, (Class) parameterTypes[i]);
                }
                result.result = handlerMethod.invoke(controller, values);
            }
            result.httpStatusCode = 200;
        }

        return result;
    }

    private static class Handler {
        Object controller;
        Method method;

        private Handler(Object controller, Method method) {
            this.controller = controller;
            this.method = method;
        }
    }
}
