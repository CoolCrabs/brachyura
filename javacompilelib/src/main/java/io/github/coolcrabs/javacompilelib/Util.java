package io.github.coolcrabs.javacompilelib;

class Util {
    // https://blog.jooq.org/2012/09/14/throw-checked-exceptions-like-runtime-exceptions-in-java/
    // The java 8 version is much nicer but whatever
    static void doThrow(Exception e) {
        Util.<RuntimeException> doThrow0(e);
    }
 
    @SuppressWarnings("unchecked")
    static <E extends Exception> void doThrow0(Exception e) throws E {
        throw (E) e;
    }
}
