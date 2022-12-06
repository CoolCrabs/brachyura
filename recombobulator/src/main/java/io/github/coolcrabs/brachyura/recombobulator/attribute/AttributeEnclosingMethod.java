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

public final class AttributeEnclosingMethod extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("EnclosingMethod");

    public static final AttributeType TYPE = new AttributeType(49, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int class_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int method_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new AttributeEnclosingMethod(
                attribute_name_index,
                class_index,
                method_index
            );
        }
    };

    public int class_index;
    public int method_index;

    public AttributeEnclosingMethod(int attribute_name_index, int class_index, int method_index) {
        super(attribute_name_index);
        this.class_index = class_index;
        this.method_index = method_index;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // class_index
        size += 2; // method_index
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) class_index);
        o.writeShort((short) method_index);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeEnclosingMethod(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) class_index;
        result = 37*result + (int) method_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeEnclosingMethod) {
            AttributeEnclosingMethod o = (AttributeEnclosingMethod) obj;
            if (
                class_index == o.class_index &&
                method_index == o.method_index &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
