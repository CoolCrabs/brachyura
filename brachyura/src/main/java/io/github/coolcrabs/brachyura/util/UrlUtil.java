package io.github.coolcrabs.brachyura.util;

public class UrlUtil {
    private UrlUtil() { }

    /**
     * pkg/yourprotocol/Handler must be a public instance of URLStreamHandler
     * Java 9 lets you use a service loader but that's no fun
     */
    public static synchronized void addHandlerPackage(String pkg) {
        String prop = System.getProperty("java.protocol.handler.pkgs");
        if (prop == null) {
            prop = pkg;
        } else {
            prop = prop + "|" + pkg;
        }
        System.setProperty("java.protocol.handler.pkgs", prop);
    }
}
