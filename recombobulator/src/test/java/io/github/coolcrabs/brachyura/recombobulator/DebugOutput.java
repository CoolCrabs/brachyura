package io.github.coolcrabs.brachyura.recombobulator;

import java.nio.ByteBuffer;

public class DebugOutput implements RecombobulatorOutput {
    RecombobulatorOutput parent;
    ByteBuffer b;
    String path;

    boolean flag = false;

    DebugOutput(ByteBuffer b, String path) {
        this.parent = RecombobulatorOutput.of(b);
        this.b = b;
        this.path = path;
    }

    void postWrite() {
        // Error at location x in file means position x + 1 = problem byte because postion is where next write wil be
        // if (!flag && "/ob.class".equals(path) && b.position() > 452) {
        //     new Throwable().printStackTrace();
        //     flag = true;
        // }
    }

    @Override
    public void writeByte(byte d) {
        parent.writeByte(d);
        postWrite();
    }

    @Override
    public void writeShort(short d) {
        parent.writeShort(d);
        postWrite();
    }

    @Override
    public void writeInt(int d) {
        parent.writeInt(d);
        postWrite();
    }

    @Override
    public void writeLong(long d) {
        parent.writeLong(d);
        postWrite();
    }

    @Override
    public void writeFloat(float d) {
        parent.writeFloat(d);
        postWrite();
    }

    @Override
    public void writeDouble(double d) {
        parent.writeDouble(d);
        postWrite();
    }

    @Override
    public void writeBytes(ByteBuffer buf) {
        parent.writeBytes(buf);
        postWrite();
    }
    
}
