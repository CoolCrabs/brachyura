package io.github.coolcrabs.fernutil;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import org.tinylog.Logger;

// Jank
class FUClassLoader extends URLClassLoader {
    static final HashMap<String, Class<?>> yeet = new HashMap<>();

    static void c(Class<?> c) {
        yeet.put(c.getName(), c);
    }
    
    static {
        c(FernUtil.class);
        c(FernUtil.LineNumbers.class);
        c(FernUtil.JavadocProvider.class);
        c(Logger.class);
    }

    FUClassLoader(URL[] classpath) {
        super(classpath, ClassLoader.getSystemClassLoader().getParent());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = yeet.get(name);
        if (c != null) {
            return c;
        }
        return super.findClass(name);
    }
}
