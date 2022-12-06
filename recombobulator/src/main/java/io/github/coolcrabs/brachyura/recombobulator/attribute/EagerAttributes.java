package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.ConstantUtf8;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

class EagerAttributes extends Attributes {
    final ArrayList<Attribute> attributes;
    final int endPos;

    EagerAttributes(ByteBuffer b, int pos, RecombobulatorOptions options, ConstantPool pool, int major, int minor, AttributeType.Location location) {
        int attributes_count = u2(b, pos);
        pos += 2;
        attributes = new ArrayList<>(attributes_count);
        for (int i = 0; i < attributes_count; i++) {
            int attribute_name_index = u2(b, pos);
            pos += 2;
            Mutf8Slice attribute_name = ((ConstantUtf8) pool.getEntry(attribute_name_index)).slice;
            AttributeType type = AttributeType.attributeMap.get(attribute_name);
            if (type == null || !type.supported(major, minor, location)) type = AttributeUnknown.TYPE;
            int attribute_length = (int) u4(b, pos); // can't be larger than max int size due to class size limit
            pos += 4;
            attributes.add(type.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor));
            pos += attribute_length;
        }
        endPos = pos;
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public Attribute get(int index) {
        return attributes.get(index);
    }

    @Override
    public void set(int index, Attribute a) {
        attributes.set(index, a);
    }

    @Override
    public void remove(int index) {
        attributes.remove(index);
    }

    @Override
    public void add(Attribute a) {
        attributes.add(a);
    }

    @Override
    public int byteSize() {
        int r = 2 /* attributes_count */ + (2 /* attribute_name_index */ + 4 /* attribute_length */) * attributes.size();
        for (Attribute a : attributes) {
            r += a.byteSize();
        }
        return r;
    }

    @Override
    public int hashCode() {
        return attributes.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attributes) {
            Attributes o = (Attributes) obj;
            if (attributes.size() != o.size()) return false;
            for (int i = 0; i < attributes.size(); i++) {
                if (!attributes.get(i).equals(o.get(i))) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int readEnd() {
        return endPos;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short)attributes.size());
        for (Attribute a : attributes) {
            o.writeShort((short) a.attribute_name_index);
            o.writeInt(a.byteSize());
            a.write(o);
        }
    }

    @Override
    public Iterator<Attribute> iterator() {
        return attributes.iterator();
    }
}
