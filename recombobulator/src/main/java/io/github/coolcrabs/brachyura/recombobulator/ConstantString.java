package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantString extends ConstantPoolEntry {
    public final int string_index;

    public ConstantString(int string_index) {
        this.string_index = string_index;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_String;
    }

    @Override
    int byteSize() {
        return 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeShort((short)string_index);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + string_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantString) {
            ConstantString o = (ConstantString) obj;
            if (string_index == o.string_index) return true;
        }
        return false;
    }
}
