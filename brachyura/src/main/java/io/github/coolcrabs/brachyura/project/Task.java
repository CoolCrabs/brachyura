package io.github.coolcrabs.brachyura.project;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class Task {
    public final String name;

    Task(String name) {
        this.name = name;
    }

    public static Task of(String name, BooleanSupplier run) {
        return new FailableNoArgTask(name, run);
    }

    public static Task of(String name, Runnable run) {
        return new NoArgTask(name, run);
    }

    public static Task of(String name, Consumer<String[]> run) {
        return new TaskWithArgs(name, run);
    }

    public abstract void doTask(String[] args);

    static class FailableNoArgTask extends Task {
        final BooleanSupplier runnable;

        FailableNoArgTask(String name, BooleanSupplier runnable) {
            super(name);
            this.runnable = runnable;
        }

        @Override
        public void doTask(String[] args) {
            if (!runnable.getAsBoolean()) System.exit(1);
        }
    }

    static class NoArgTask extends Task {
        final Runnable runnable;

        NoArgTask(String name, Runnable runnable) {
            super(name);
            this.runnable = runnable;
        }

        @Override
        public void doTask(String[] args) {
            runnable.run();
        }
    }

    static class TaskWithArgs extends Task {
        final Consumer<String[]> task;

        TaskWithArgs(String name, Consumer<String[]> task) {
            super(name);
            this.task = task;
        }

        @Override
        public void doTask(String[] args) {
            task.accept(args);
        }
    }
}
