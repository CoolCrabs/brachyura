package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantLong extends ConstantPoolEntry {
    public final long data;

    public ConstantLong(long data) {
        this.data = data;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_Long;
    }

    @Override
    int byteSize() {
        return 8;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeLong(data);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) (data^(data>>>32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantLong) {
            ConstantLong o = (ConstantLong) obj;
            if (data == o.data) return true;
        }
        return false;
    }
}
