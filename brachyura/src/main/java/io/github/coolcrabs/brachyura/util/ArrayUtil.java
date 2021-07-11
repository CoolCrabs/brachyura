package io.github.coolcrabs.brachyura.util;

import java.lang.reflect.Array;

public class ArrayUtil {
    private ArrayUtil() { }

    @SuppressWarnings("unchecked")
    public static <T> T[] join(Class<T> clazz, T[][] in) {
        int len = 0;
        for (T[] a : in) len += a.length;
        T[] out = (T[]) Array.newInstance(clazz, len);
        int p = 0;
        for (T[] a : in) {
            for (int i = 0; i < a.length; i++) {
                out[p] = a[i];
                ++p;
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] join(Class<T> clazz, T[] inA, T... inB) {
        int len = inA.length + inB.length;
        T[] out = (T[]) Array.newInstance(clazz, len);
        int p = 0;
        for (int i = 0; i < inA.length; i++) {
            out[p] = inA[i];
            ++p;
        }
        for (int i = 0; i < inB.length; i++) {
            out[p] = inB[i];
            ++p;
        }
        return out;
    }
}
