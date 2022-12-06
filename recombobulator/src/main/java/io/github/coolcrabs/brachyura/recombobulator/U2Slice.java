package io.github.coolcrabs.brachyura.recombobulator;

import java.nio.ByteBuffer;

public class U2Slice {
    public final ByteBuffer b;

    public U2Slice(ByteBuffer b) {
        this.b = b;
    }

    public int byteSize() {
        return b.remaining();
    }

    public int size() {
        return b.remaining() / 2;
    }

    public int get(int index) {
        return ByteBufferUtil.u2(b, b.position() + index * 2);
    }

    public void put(int index, int value) {
        b.putShort(b.position() + index * 2, (short) value);
    }

    public void write(RecombobulatorOutput o) {
        o.writeBytes(b);
    }
}
