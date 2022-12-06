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

public final class AttributeLocalVariableTypeTable extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("LocalVariableTypeTable");

    public static final AttributeType TYPE = new AttributeType(49, 0, EnumSet.of(AttributeType.Location.CODE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int local_variable_type_table_length = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryLocalVariableTypeTable> local_variable_type_table = new ArrayList<>(local_variable_type_table_length);
            for (int i = 0; i < local_variable_type_table_length; i++) {
                EntryLocalVariableTypeTable tmp = EntryLocalVariableTypeTable.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                local_variable_type_table.add(tmp);
            }
            return new AttributeLocalVariableTypeTable(
                attribute_name_index,
                local_variable_type_table
            );
        }
    };

    public List<EntryLocalVariableTypeTable> local_variable_type_table;

    public AttributeLocalVariableTypeTable(int attribute_name_index, List<EntryLocalVariableTypeTable> local_variable_type_table) {
        super(attribute_name_index);
        this.local_variable_type_table = local_variable_type_table;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // local_variable_type_table_length
        for (EntryLocalVariableTypeTable tmp : local_variable_type_table) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) local_variable_type_table.size());
        for (EntryLocalVariableTypeTable tmp : local_variable_type_table) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeLocalVariableTypeTable(this);
        for (EntryLocalVariableTypeTable tmp : local_variable_type_table) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + local_variable_type_table.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeLocalVariableTypeTable) {
            AttributeLocalVariableTypeTable o = (AttributeLocalVariableTypeTable) obj;
            if (
                local_variable_type_table.equals(o.local_variable_type_table) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
