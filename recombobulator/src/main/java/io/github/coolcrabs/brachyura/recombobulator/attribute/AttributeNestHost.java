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

public final class AttributeNestHost extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("NestHost");

    public static final AttributeType TYPE = new AttributeType(55, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int host_class_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new AttributeNestHost(
                attribute_name_index,
                host_class_index
            );
        }
    };

    public int host_class_index;

    public AttributeNestHost(int attribute_name_index, int host_class_index) {
        super(attribute_name_index);
        this.host_class_index = host_class_index;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // host_class_index
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) host_class_index);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeNestHost(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) host_class_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeNestHost) {
            AttributeNestHost o = (AttributeNestHost) obj;
            if (
                host_class_index == o.host_class_index &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
