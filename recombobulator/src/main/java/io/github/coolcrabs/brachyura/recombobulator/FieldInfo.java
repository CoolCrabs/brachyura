package io.github.coolcrabs.brachyura.recombobulator;

import java.nio.ByteBuffer;

import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeType;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Attributes;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

public final class FieldInfo {
    public int access_flags;
    public int name_index;
    public int descriptor_index;
    public Attributes attributes;

    FieldInfo() { }

    int read(ByteBuffer b, int pos, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
        access_flags = u2(b, pos);
        pos += 2;
        name_index = u2(b, pos);
        pos += 2;
        descriptor_index = u2(b, pos);
        pos += 2;
        attributes = Attributes.read(b, pos, options, pool, major, minor, AttributeType.Location.FIELD_INFO);
        return attributes.readEnd();
    }

    int byteSize() {
        return 2 + // access_flags
            2 + // name_index
            2 + // descriptor_index
            attributes.byteSize();
    }

    void write(RecombobulatorOutput o) {
        o.writeShort((short)access_flags);
        o.writeShort((short)name_index);
        o.writeShort((short)descriptor_index);
        attributes.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitFieldInfo(this);
        for (int i = 0; i < attributes.size(); i++) attributes.get(i).accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + access_flags;
        result = 37*result + name_index;
        result = 37*result + descriptor_index;
        result = 37*result + access_flags;
        result = 37*result + attributes.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof FieldInfo) {
            FieldInfo o = (FieldInfo) obj;
            return access_flags == o.access_flags &&
                name_index == o.name_index &&
                descriptor_index == o.descriptor_index &&
                attributes.equals(o.attributes);
        }
        return false;
    }
}
