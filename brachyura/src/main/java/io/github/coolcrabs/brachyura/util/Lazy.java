package io.github.coolcrabs.brachyura.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

// Based on https://dzone.com/articles/be-lazy-with-java-8
// Modified to take the supplier in the constructor
// and support evaluating in parallel
@SuppressWarnings("all")
public final class Lazy<T> implements Supplier<T> {

    private volatile T value;
    private volatile ForkJoinTask<T> task;
    private final Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        final T result = value; // Just one volatile read 
        return result == null ? maybeCompute() : result;
    }

    private synchronized T maybeCompute() {
        if (task != null) {
            value = Objects.requireNonNull(task.join());
        } else if (value == null) {
            value = Objects.requireNonNull(supplier.get());
        }
        return value;
    }

    public static <T> List<T> getParallel(Collection<Lazy<T>> things) {
        return getParallel(things.toArray(new Lazy[0]));
    }

    public static <T> List<T> getParallel(Lazy<T>... things) {
        class Task {
            int slot;
            ForkJoinTask<T> task;
        }
        ArrayList<T> result = new ArrayList<>(things.length);
        ArrayList<Task> tasks = new ArrayList<>(things.length);
        for (int i = 0; i < things.length; i++) {
            Lazy<T> thing = things[i];
            T v = thing.value;
            if (v != null) {
                result.add(v);
            } else {
                ForkJoinTask<T> task = thing.task;
                if (task == null) {
                    synchronized (thing) {
                        v = thing.value;
                        if (v != null) {
                            result.add(v);
                            continue;
                        }
                        task = thing.task;
                        if (task == null) {
                            task = ForkJoinTask.adapt(thing.supplier::get);
                            task.fork();
                            thing.task = task;
                        }
                    }
                }
                result.add(null);
                Task t = new Task();
                t.slot = i;
                t.task = task;
                tasks.add(t);
            }
        }
        for (Task t : tasks) {
            result.set(t.slot, t.task.join());
        }
        return result;
    }

}
