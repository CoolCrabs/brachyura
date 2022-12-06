package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantInteger extends ConstantPoolEntry {
    public final int data;

    public ConstantInteger(int data) {
        this.data = data;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_Integer;
    }

    @Override
    int byteSize() {
        return 4;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeInt(data);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + data;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantInteger) {
            ConstantInteger o = (ConstantInteger) obj;
            if (data == o.data) return true;
        }
        return false;
    }
}
