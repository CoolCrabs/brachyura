package io.github.coolcrabs.brachyura.util;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.concurrent.ConcurrentHashMap;

public class UrlUtil {
    private UrlUtil() { }
    private static ConcurrentHashMap<String, URLStreamHandler> handlers = new ConcurrentHashMap<>();

    static {
        URL.setURLStreamHandlerFactory(StreamHandlerFactory.INSTANCE);
    }

    public static void addHandler(String protocol, URLStreamHandler handler) {
        handlers.put(protocol, handler);
    }

    enum StreamHandlerFactory implements URLStreamHandlerFactory {
        INSTANCE;

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            return handlers.get(protocol);
        }
    }
}
