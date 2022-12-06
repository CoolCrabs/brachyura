package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantFloat extends ConstantPoolEntry {
    public final float data;

    public ConstantFloat(float data) {
        this.data = data;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_Float;
    }

    @Override
    int byteSize() {
        return 4;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeFloat(data);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + Float.floatToIntBits(data);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantFloat) {
            ConstantFloat o = (ConstantFloat) obj;
            if (data == o.data) return true;
        }
        return false;
    }
}
