package io.github.coolcrabs.brachyura.recombobulator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeType;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Attributes;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

public final class ClassInfo {
    public int minor_version;
    public int major_version;
    public ConstantPool pool;
    public int access_flags;
    public int this_class;
    public int super_class;
    public int[] interfaces;
    public List<FieldInfo> fields;
    public List<MethodInfo> methods;
    public Attributes attributes;

    public ClassInfo(ByteBuffer b, RecombobulatorOptions options) {
        b.order(ByteOrder.BIG_ENDIAN);
        int pos = b.position();
        if (b.getInt(pos) != 0xCAFEBABE) throw new ClassDecodeException("Illegal class magic");
        pos += 4;
        minor_version = u2(b, pos);
        pos += 2;
        major_version = u2(b, pos);
        pos += 2;
        pool = new ConstantPool();
        pos = pool.read(b, 8);
        access_flags = u2(b, pos);
        pos += 2;
        this_class = u2(b, pos);
        pos += 2;
        super_class = u2(b, pos);
        pos += 2;
        int interfaces_count = u2(b, pos);
        interfaces = new int[interfaces_count];
        pos += 2;
        for (int i = 0; i < interfaces_count; i++) {
            interfaces[i] = u2(b, pos);
            pos += 2;
        }
        int fields_count = u2(b, pos);
        pos += 2;
        fields = new ArrayList<>(fields_count);
        for (int i = 0; i < fields_count; i++) {
            FieldInfo f = new FieldInfo();
            pos = f.read(b, pos, options, pool, major_version, minor_version);
            fields.add(f);
        }
        int methods_count = u2(b, pos);
        pos += 2;
        methods = new ArrayList<>(methods_count);
        for (int i = 0; i < methods_count; i++) {
            MethodInfo m = new MethodInfo();
            pos = m.read(b, pos, options, pool, major_version, minor_version);
            methods.add(m);
        }
        attributes = Attributes.read(b, pos, options, pool, major_version, minor_version, AttributeType.Location.CLASS_FILE);
    }

    public void write(RecombobulatorOutput o) {
        o.writeInt(0xCAFEBABE);
        o.writeShort((short)minor_version);
        o.writeShort((short)major_version);
        pool.write(o);
        o.writeShort((short)access_flags);
        o.writeShort((short)this_class);
        o.writeShort((short)super_class);
        o.writeShort((short)interfaces.length);
        for (int i : interfaces) {
            o.writeShort((short)i);
        }
        o.writeShort((short)fields.size());
        for (FieldInfo f : fields) {
            f.write(o);
        }
        o.writeShort((short)methods.size());
        for (MethodInfo m : methods) {
            m.write(o);
        }
        attributes.write(o);
    }

    public int byteSize() {
        int size =
            4 + // magic
            2 + // minor_version
            2 + // major_version
            2; // constant_pool_count
        for (ConstantPoolEntry e : pool.pool) {
            if (e instanceof ConstantUnusable) continue;
            size += 1;
            size += e.byteSize();
        }
        size +=
            2 + // access_flags
            2 + // this_class
            2 + // super_class
            2 + // interfaces_count
            (2 * interfaces.length) + // interfaces
            2; // fields_count
        for (FieldInfo f : fields) {
            size += f.byteSize();
        }
        size += 2; // methods_count
        for (MethodInfo m : methods) {
            size += m.byteSize();
        }
        size += attributes.byteSize();
        return size;
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitClassInfo(this);
        for (FieldInfo f : fields) f.accept(v);
        for (MethodInfo m : methods) m.accept(v);
        for (int i = 0; i < attributes.size(); i++) attributes.get(i).accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + minor_version;
        result = 37*result + major_version;
        result = 37*result + pool.hashCode();
        result = 37*result + access_flags;
        result = 37*result + this_class;
        result = 37*result + super_class;
        result = 37*result + Arrays.hashCode(interfaces);
        result = 37*result + fields.hashCode();
        result = 37*result + methods.hashCode();
        result = 37*result + attributes.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ClassInfo)) return false;
        ClassInfo o = (ClassInfo) obj;
        if (
            minor_version == o.minor_version &&
            major_version == o.major_version &&
            pool.size() == o.pool.size() &&
            access_flags == o.access_flags &&
            this_class == o.this_class &&
            super_class == o.super_class &&
            interfaces.length == o.interfaces.length &&
            fields.size() == o.fields.size() &&
            methods.size() == o.methods.size() &&
            attributes.size() == o.attributes.size()
        ) {
            if (!pool.equals(o.pool)) return false;
            if (!Arrays.equals(interfaces, o.interfaces)) return false;
            if (!fields.equals(o.fields)) return false;
            if (!attributes.equals(o.attributes)) return false;
            return true;
        }
        return false;
    }
}
