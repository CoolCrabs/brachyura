package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantMethodHandle extends ConstantPoolEntry {
    public static final byte REF_getField = 1;
    public static final byte REF_getStatic = 2;
    public static final byte REF_putField = 3;
    public static final byte REF_putStatic = 4;
    public static final byte REF_invokeVirtual = 5;
    public static final byte REF_invokeStatic = 6;
    public static final byte REF_invokeSpecial = 7;
    public static final byte REF_newInvokeSpecial = 8;
    public static final byte REF_invokeInterface = 9;

    public final byte reference_kind;
    public final int reference_index;

    public ConstantMethodHandle(byte reference_kind, int reference_index) {
        this.reference_kind = reference_kind;
        this.reference_index = reference_index;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_MethodHandle;
    }

    @Override
    int byteSize() {
        return 1 /* reference_kind */ + 2 /* reference_index */;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeByte(reference_kind);
        o.writeShort((short)reference_index);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + reference_kind;
        result = 37*result + reference_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantMethodHandle) {
            ConstantMethodHandle o = (ConstantMethodHandle) obj;
            if (
                reference_kind == o.reference_kind &&
                reference_index == o.reference_index
            ) return true;
        }
        return false;
    }
}
