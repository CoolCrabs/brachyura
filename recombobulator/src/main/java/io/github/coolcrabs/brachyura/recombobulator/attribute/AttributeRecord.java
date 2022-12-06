package io.github.coolcrabs.brachyura.recombobulator.attribute;

// GENERATED CLASS :)

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;
import io.github.coolcrabs.brachyura.recombobulator.U2Slice;

public final class AttributeRecord extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("Record");

    public static final AttributeType TYPE = new AttributeType(60, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int components_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<RecordComponentInfo> components = new ArrayList<>(components_count);
            for (int i = 0; i < components_count; i++) {
                RecordComponentInfo tmp = new RecordComponentInfo();
                pos = tmp.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                components.add(tmp);
            }
            return new AttributeRecord(
                attribute_name_index,
                components
            );
        }
    };

    public List<RecordComponentInfo> components;

    public AttributeRecord(int attribute_name_index, List<RecordComponentInfo> components) {
        super(attribute_name_index);
        this.components = components;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // components_count
        for (RecordComponentInfo tmp : components) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) components.size());
        for (RecordComponentInfo tmp : components) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeRecord(this);
        for (RecordComponentInfo tmp : components) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + components.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeRecord) {
            AttributeRecord o = (AttributeRecord) obj;
            if (
                components.equals(o.components) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
