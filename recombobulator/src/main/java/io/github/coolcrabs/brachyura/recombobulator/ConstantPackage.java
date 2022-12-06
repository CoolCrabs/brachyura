package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantPackage extends ConstantPoolEntry {
    public final int name_index;

    public ConstantPackage(int name_index) {
        this.name_index = name_index;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_Package;
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
        if (obj instanceof ConstantPackage) {
            ConstantPackage o = (ConstantPackage) obj;
            if (name_index == o.name_index) return true;
        }
        return false;
    }
}
