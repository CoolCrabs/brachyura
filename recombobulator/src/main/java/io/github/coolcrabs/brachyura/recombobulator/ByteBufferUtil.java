package io.github.coolcrabs.brachyura.recombobulator;

import java.nio.ByteBuffer;

public class ByteBufferUtil {
    private ByteBufferUtil() { }

    public static int u1(ByteBuffer b, int pos) {
        return b.get(pos) & 0xFF;
    }

    public static int u2(ByteBuffer b, int pos) {
        return b.getShort(pos) & 0xFFFF;
    }

    public static long u4(ByteBuffer b, int pos) {
        return b.getInt(pos) & 0xFFFFFFFFl;
    }

    public static ByteBuffer slice(ByteBuffer b, int start, int end) {
        ByteBuffer n = b.duplicate();
        n.position(start);
        n.limit(end);
        return n;
    }
}
