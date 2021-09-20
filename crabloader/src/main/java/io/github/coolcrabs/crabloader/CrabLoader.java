package io.github.coolcrabs.crabloader;

import java.io.DataInputStream;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * System property crabloader.config specifies which config to use.
 * That config should be in the file "crabloaderconf" + config + ".bin" in the ide's run classpath.
 * 
 * Binary Format:
 * 
 * String targetClass
 * int cpEntries
 * String[cpEntries] urls
 * int argEntries
 * String[argEntries] args
 * int propertyEntries
 * String[propertyEntries * 2] properties
 */
public class CrabLoader {
    public static void main(String[] args) throws Throwable {
        String config = System.getProperty("crabloader.config");
        Objects.requireNonNull(config);
        ClassLoader system = ClassLoader.getSystemClassLoader();
        String targetClass;
        URL[] urls;
        String[] targetArgs;
        try (DataInputStream is = new DataInputStream(system.getResourceAsStream("crabloaderconf" + config + ".bin"))) {
            targetClass = is.readUTF(); 
            int cpEntries = is.readInt();
            urls = new URL[cpEntries];
            for (int i = 0; i < cpEntries; i++) {
                urls[i] = new URL(is.readUTF());
            }
            int argEntries = is.readInt();
            targetArgs = new String[argEntries];
            for (int i = 0; i < argEntries; i++) {
                targetArgs[i] = is.readUTF();
            }
            int propertyEntries = is.readInt(); // 2 Strings per entry
            for (int i = 0; i < propertyEntries; i++) {
                System.setProperty(is.readUTF(), is.readUTF());
            }
        }
        URLClassLoader crab = new URLClassLoader(urls, system);
        Thread.currentThread().setContextClassLoader(crab);
        updateClasspathSystemProp(urls);
        Class<?> target = Class.forName(targetClass, true, crab);
        MethodHandles.publicLookup().findStatic(
            target,
            "main",
            MethodType.methodType(void.class, String[].class)
        ).invokeExact(targetArgs);
    }

    private static void updateClasspathSystemProp(URL[] append) throws URISyntaxException {
        String existing = System.getProperty("java.class.path");
        String seperator = System.getProperty("path.separator");
        StringBuilder builder = new StringBuilder();
        boolean e = existing != null && !existing.isEmpty();
        if (e) {
            builder.append(existing);
        }
        for (URL url : append) {
            if (e) {
                builder.append(seperator);
            } else {
                e = true;
            }
            builder.append(Paths.get(url.toURI()));
        }
        System.setProperty("java.class.path", builder.toString());
    }
}
