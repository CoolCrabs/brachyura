package io.github.coolcrabs.brachyura.recombobulator;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

/**
 * 1 indexed!!!!
 */
public class ConstantPool {
    ArrayList<ConstantPoolEntry> pool;

    ConstantPool() { }

    public ConstantPoolEntry getEntry(int index) {
        return pool.get(index - 1);
    }

    public void setEntry(int index, ConstantPoolEntry entry) {
        index -= 1; // zero index
        if (index == pool.size()) {
            pool.add(entry);
        } else if (index < pool.size()) {
            ConstantPoolEntry oldEntry = pool.set(index, entry);
            if (oldEntry instanceof ConstantDouble || oldEntry instanceof ConstantLong) {
                pool.set(index + 1, new ConstantInteger(1234567));
            } else if (oldEntry == ConstantUnusable.INSTANCE) {
                throw new IllegalArgumentException("Cannot remove unusable constant pool entry at index" + index);
            }
        } else {
            throw new IllegalArgumentException("Out of pool index " + index);
        }
    }

    public ConstantPool duplicate() {
        ConstantPool o = new ConstantPool();
        o.pool = new ArrayList<>(pool);
        return o;
    }

    public int size() {
        return pool.size();
    }

    @Override
    public int hashCode() {
        return pool.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantPool) {
            return pool.equals(((ConstantPool)obj).pool);
        }
        return false;
    }

    static final int CONSTANT_Utf8 = 1;
    static final int CONSTANT_Integer = 3;
    static final int CONSTANT_Float = 4;
    static final int CONSTANT_Long = 5;
    static final int CONSTANT_Double = 6;
    static final int CONSTANT_Class = 7;
    static final int CONSTANT_String = 8;
    static final int CONSTANT_Fieldref = 9;
    static final int CONSTANT_Methodref = 10;
    static final int CONSTANT_InterfaceMethodref = 11;
    static final int CONSTANT_NameAndType = 12;
    static final int CONSTANT_MethodHandle = 15;
    static final int CONSTANT_MethodType = 16;
    static final int CONSTANT_Dynamic = 17;
    static final int CONSTANT_InvokeDynamic = 18;
    static final int CONSTANT_Module = 19;
    static final int CONSTANT_Package = 20;

    int read(ByteBuffer b, int pos) {
        int constant_pool_count = u2(b, pos);
        pos += 2;
        pool = new ArrayList<>(constant_pool_count - 1);
        for (int i = 1; i < constant_pool_count; i++) { // loop constant_pool_count - 1 times 
            int tag = u1(b, pos);
            pos += 1;
            switch (tag) {
                case CONSTANT_Utf8:
                    int length = u2(b, pos);
                    pos += 2;
                    pool.add(new ConstantUtf8(new Mutf8Slice(slice(b, pos, pos + length))));
                    pos += length;
                    break;
                case CONSTANT_Integer:
                    pool.add(new ConstantInteger(b.getInt(pos)));
                    pos += 4;
                    break;
                case CONSTANT_Float:
                    pool.add(new ConstantFloat(b.getFloat(pos)));
                    pos += 4;
                    break;
                case CONSTANT_Long:
                    pool.add(new ConstantLong(b.getLong(pos)));
                    pool.add(ConstantUnusable.INSTANCE);
                    pos += 8;
                    i += 1;
                    break;
                case CONSTANT_Double:
                    pool.add(new ConstantDouble(b.getDouble(pos)));
                    pool.add(ConstantUnusable.INSTANCE);
                    pos += 8;
                    i += 1;
                    break;
                case CONSTANT_Class:
                    pool.add(new ConstantClass(u2(b, pos)));
                    pos += 2;
                    break;
                case CONSTANT_String:
                    pool.add(new ConstantString(u2(b, pos)));
                    pos += 2;
                    break;
                case CONSTANT_Fieldref:
                    pool.add(new ConstantFieldref(u2(b, pos), u2(b, pos + 2)));
                    pos += 4;
                    break;
                case CONSTANT_Methodref:
                    pool.add(new ConstantMethodref(u2(b, pos), u2(b, pos + 2)));
                    pos += 4;
                    break;
                case CONSTANT_InterfaceMethodref:
                    pool.add(new ConstantInterfaceMethodref(u2(b, pos), u2(b, pos + 2)));
                    pos += 4;
                    break;
                case CONSTANT_NameAndType:
                    pool.add(new ConstantNameAndType(u2(b, pos), u2(b, pos + 2)));
                    pos += 4;
                    break;
                case CONSTANT_MethodHandle:
                    pool.add(new ConstantMethodHandle(b.get(pos), u2(b, pos + 1)));
                    pos += 3;
                    break;
                case CONSTANT_MethodType:
                    pool.add(new ConstantMethodType(u2(b, pos)));
                    pos += 2;
                    break;
                case CONSTANT_Dynamic:
                    pool.add(new ConstantDynamic(u2(b, pos), u2(b, pos + 2)));
                    pos += 4;
                    break;
                case CONSTANT_InvokeDynamic:
                    pool.add(new ConstantInvokeDynamic(u2(b, pos), u2(b, pos + 2)));
                    pos += 4;
                    break;
                case CONSTANT_Module:
                    pool.add(new ConstantModule(u2(b, pos)));
                    pos += 2;
                    break;
                case CONSTANT_Package:
                    pool.add(new ConstantPackage(u2(b, pos)));
                    pos += 2;
                    break;
                default:
                    throw new ClassDecodeException("Unknown constant tag: " + tag);
            }
        }
        return pos;
    }

    void write(RecombobulatorOutput o) {
        o.writeShort((short)(size() + 1));
        for (int i = 0; i < pool.size(); i++) {
            ConstantPoolEntry e = pool.get(i);
            if (e instanceof ConstantUnusable) continue;
            o.writeByte(e.tag());
            e.write(o);
        }
    }
}
