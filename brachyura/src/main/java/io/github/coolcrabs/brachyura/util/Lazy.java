package io.github.coolcrabs.brachyura.util;

import java.util.Objects;
import java.util.function.Supplier;

// Based on https://dzone.com/articles/be-lazy-with-java-8
// Modified to take the supplier in the constructor
@SuppressWarnings("all")
public final class Lazy<T> {

    private volatile T value;
    private final Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        final T result = value; // Just one volatile read 
        return result == null ? maybeCompute() : result;
    }

    private synchronized T maybeCompute() {
        if (value == null) {
            value = Objects.requireNonNull(supplier.get());
        }
        return value;
    }

}
