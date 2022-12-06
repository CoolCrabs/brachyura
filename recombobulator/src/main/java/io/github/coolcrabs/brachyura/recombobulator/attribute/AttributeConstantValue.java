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

public final class AttributeConstantValue extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("ConstantValue");

    public static final AttributeType TYPE = new AttributeType(45, 3, EnumSet.of(AttributeType.Location.FIELD_INFO)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int constantvalue_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new AttributeConstantValue(
                attribute_name_index,
                constantvalue_index
            );
        }
    };

    public int constantvalue_index;

    public AttributeConstantValue(int attribute_name_index, int constantvalue_index) {
        super(attribute_name_index);
        this.constantvalue_index = constantvalue_index;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // constantvalue_index
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) constantvalue_index);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeConstantValue(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) constantvalue_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeConstantValue) {
            AttributeConstantValue o = (AttributeConstantValue) obj;
            if (
                constantvalue_index == o.constantvalue_index &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
