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

public final class AttributeExceptions extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("Exceptions");

    public static final AttributeType TYPE = new AttributeType(45, 3, EnumSet.of(AttributeType.Location.METHOD_INFO)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int number_of_exceptions = ByteBufferUtil.u2(b, pos);
            pos += 2;
            U2Slice exception_index_table = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (number_of_exceptions * 2)));
            pos += number_of_exceptions * 2;
            return new AttributeExceptions(
                attribute_name_index,
                exception_index_table
            );
        }
    };

    public U2Slice exception_index_table;

    public AttributeExceptions(int attribute_name_index, U2Slice exception_index_table) {
        super(attribute_name_index);
        this.exception_index_table = exception_index_table;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // number_of_exceptions
        size += exception_index_table.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) exception_index_table.size());
        exception_index_table.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeExceptions(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + exception_index_table.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeExceptions) {
            AttributeExceptions o = (AttributeExceptions) obj;
            if (
                exception_index_table.equals(o.exception_index_table) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
