package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantMethodType extends ConstantPoolEntry {
    public final int descriptor_index;

    public ConstantMethodType(int descriptor_index) {
        this.descriptor_index = descriptor_index;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_MethodType;
    }

    @Override
    int byteSize() {
        return 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeShort((short)descriptor_index);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + descriptor_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantMethodType) {
            ConstantMethodType o = (ConstantMethodType) obj;
            if (descriptor_index == o.descriptor_index) return true;
        }
        return false;
    }
}
