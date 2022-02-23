package io.github.coolcrabs.brachyura.memurl;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.util.UrlUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class MemoryUrlProvider implements AutoCloseable {
    static {
        UrlUtil.addHandlerPackage("io.github.coolcrabs.brachyura");
    }

    static final AtomicInteger idSup = new AtomicInteger(0);
    static final ConcurrentHashMap<String, MemoryUrlProvider> instances = new ConcurrentHashMap<>();

    final String id;
    final Function<String, Supplier<InputStream>> func;

    public MemoryUrlProvider(Function<String, Supplier<InputStream>> func) {
        this.id = "MemUrlProvider" + idSup.getAndIncrement();
        this.func = func;
        instances.put(id, this);
    }

    public URL getRootUrl() {
        try {
            return new URL("memurl", id, "/");
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    @Override
    public void close() {
        instances.remove(this.id);
    }
}
