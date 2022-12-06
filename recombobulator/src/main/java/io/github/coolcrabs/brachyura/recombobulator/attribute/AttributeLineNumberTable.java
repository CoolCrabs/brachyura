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

public final class AttributeLineNumberTable extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("LineNumberTable");

    public static final AttributeType TYPE = new AttributeType(45, 3, EnumSet.of(AttributeType.Location.CODE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int line_number_table_length = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryLineNumberTable> line_number_table = new ArrayList<>(line_number_table_length);
            for (int i = 0; i < line_number_table_length; i++) {
                EntryLineNumberTable tmp = EntryLineNumberTable.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                line_number_table.add(tmp);
            }
            return new AttributeLineNumberTable(
                attribute_name_index,
                line_number_table
            );
        }
    };

    public List<EntryLineNumberTable> line_number_table;

    public AttributeLineNumberTable(int attribute_name_index, List<EntryLineNumberTable> line_number_table) {
        super(attribute_name_index);
        this.line_number_table = line_number_table;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // line_number_table_length
        for (EntryLineNumberTable tmp : line_number_table) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) line_number_table.size());
        for (EntryLineNumberTable tmp : line_number_table) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeLineNumberTable(this);
        for (EntryLineNumberTable tmp : line_number_table) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + line_number_table.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeLineNumberTable) {
            AttributeLineNumberTable o = (AttributeLineNumberTable) obj;
            if (
                line_number_table.equals(o.line_number_table) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
