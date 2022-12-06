package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantClass extends ConstantPoolEntry {
    public final int name_index;

    public ConstantClass(int name_index) {
        this.name_index = name_index;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_Class;
    }

    @Override
    int byteSize() {
        return 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeShort((short)name_index);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + name_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantClass) {
            ConstantClass o = (ConstantClass) obj;
            if (name_index == o.name_index) return true;
        }
        return false;
    }
}
