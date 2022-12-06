package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantMethodref extends ConstantPoolEntry {
    public final int class_index;
    public final int name_and_type_index;

    public ConstantMethodref(int class_index, int name_and_type_index) {
        this.class_index = class_index;
        this.name_and_type_index = name_and_type_index;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_Methodref;
    }

    @Override
    int byteSize() {
        return 2 /* class_index */ + 2 /* name_and_type_index */;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeShort((short) class_index);
        o.writeShort((short) name_and_type_index);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + class_index;
        result = 37*result + name_and_type_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantMethodref) {
            ConstantMethodref o = (ConstantMethodref) obj;
            if (
                class_index == o.class_index &&
                name_and_type_index == o.name_and_type_index
            ) return true;
        }
        return false;
    }
}

