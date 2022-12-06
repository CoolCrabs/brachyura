package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantNameAndType extends ConstantPoolEntry {
    public final int name_index;
    public final int descriptor_index;

    public ConstantNameAndType(int name_index, int descriptor_index) {
        this.name_index = name_index;
        this.descriptor_index = descriptor_index;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_NameAndType;
    }

    @Override
    int byteSize() {
        return 2 /* name_index */ + 2 /* descriptor_index */;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeShort((short)name_index);
        o.writeShort((short)descriptor_index);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + name_index;
        result = 37*result + descriptor_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantNameAndType) {
            ConstantNameAndType o = (ConstantNameAndType) obj;
            if (
                name_index == o.name_index &&
                descriptor_index == o.descriptor_index
            ) return true;
        }
        return false;
    }
}
