package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantInvokeDynamic extends ConstantPoolEntry {
    public final int bootstrap_method_attr_index;
    public final int name_and_type_index;

    public ConstantInvokeDynamic(int bootstrap_method_attr_index, int name_and_type_index) {
        this.bootstrap_method_attr_index = bootstrap_method_attr_index;
        this.name_and_type_index = name_and_type_index;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_InvokeDynamic;
    }

    @Override
    int byteSize() {
        return 2 /* bootstrap_method_attr_index */ + 2 /* name_and_type_index */;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeShort((short)bootstrap_method_attr_index);
        o.writeShort((short)name_and_type_index);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + bootstrap_method_attr_index;
        result = 37*result + name_and_type_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantInvokeDynamic) {
            ConstantInvokeDynamic o = (ConstantInvokeDynamic) obj;
            if (
                bootstrap_method_attr_index == o.bootstrap_method_attr_index &&
                name_and_type_index == o.name_and_type_index
            ) return true;
        }
        return false;
    }
}
