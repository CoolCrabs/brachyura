package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;

import io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class RecordComponentInfo {
    public int name_index;
    public int descriptor_index;
    public Attributes attributes;

    public int read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
        name_index = ByteBufferUtil.u2(b, pos);
        pos += 2;
        descriptor_index = ByteBufferUtil.u2(b, pos);
        pos += 2;
        attributes = Attributes.read(b, pos, options, pool, major, minor, AttributeType.Location.RECORD_COMPONENT);
        return attributes.readEnd();
    }

    public int byteSize() {
        int size = 0;
        size += 2; // name_index
        size += 2; // descriptor_index
        size += attributes.byteSize();
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) name_index);
        o.writeShort((short) descriptor_index);
        attributes.write(o);
    }
    
    public void accept(RecombobulatorVisitor v) {
        v.visitRecordComponentInfo(this);
        for (int i = 0; i < attributes.size(); i++) attributes.get(i).accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + name_index;
        result = 37*result + descriptor_index;
        result = 37*result + attributes.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof RecordComponentInfo) {
            RecordComponentInfo o = (RecordComponentInfo) obj;
            if (
                name_index == o.name_index &&
                descriptor_index == o.descriptor_index &&
                attributes.equals(o.attributes)
            ) return true;
        }
        return false;
    }
}
