package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantUtf8 extends ConstantPoolEntry {
    public final Mutf8Slice slice;

    public ConstantUtf8(Mutf8Slice slice) {
        this.slice = slice;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_Utf8;
    }

    @Override
    int byteSize() {
        return 2 /* length prefix */ + slice.b.remaining() /* data */;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeShort((short)slice.b.remaining());
        o.writeBytes(slice.b);
    }

    @Override
    public int hashCode() {
        return slice.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ConstantUtf8) && slice.equals(((ConstantUtf8)obj).slice);
    }
}
