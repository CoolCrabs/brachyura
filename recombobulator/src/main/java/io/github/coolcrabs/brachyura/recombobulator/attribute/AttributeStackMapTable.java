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

public final class AttributeStackMapTable extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("StackMapTable");

    public static final AttributeType TYPE = new AttributeType(50, 0, EnumSet.of(AttributeType.Location.CODE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int number_of_entries = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<StackMapFrame> entries = new ArrayList<>(number_of_entries);
            pos += StackMapFrame.read(b, pos, number_of_entries, entries);
            return new AttributeStackMapTable(
                attribute_name_index,
                entries
            );
        }
    };

    public List<StackMapFrame> entries;

    public AttributeStackMapTable(int attribute_name_index, List<StackMapFrame> entries) {
        super(attribute_name_index);
        this.entries = entries;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // number_of_entries
        for (StackMapFrame tmp : entries) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) entries.size());
        for (StackMapFrame tmp : entries) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeStackMapTable(this);
        for (StackMapFrame tmp : entries) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + entries.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeStackMapTable) {
            AttributeStackMapTable o = (AttributeStackMapTable) obj;
            if (
                entries.equals(o.entries) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
