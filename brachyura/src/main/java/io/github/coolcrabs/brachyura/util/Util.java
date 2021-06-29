package io.github.coolcrabs.brachyura.util;

public class Util {
    private Util() { }

    @SuppressWarnings("all")
    public static <T extends Throwable> RuntimeException sneak(Throwable t) throws T {
        throw (T)t;
    }

    public static <T extends Throwable> void unsneak() throws T {
        //noop
    }
}
