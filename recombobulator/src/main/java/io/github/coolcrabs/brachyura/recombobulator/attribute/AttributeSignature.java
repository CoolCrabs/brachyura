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

public final class AttributeSignature extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("Signature");

    public static final AttributeType TYPE = new AttributeType(49, 0, EnumSet.of(AttributeType.Location.CLASS_FILE, AttributeType.Location.FIELD_INFO, AttributeType.Location.METHOD_INFO, AttributeType.Location.RECORD_COMPONENT)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int signature_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new AttributeSignature(
                attribute_name_index,
                signature_index
            );
        }
    };

    public int signature_index;

    public AttributeSignature(int attribute_name_index, int signature_index) {
        super(attribute_name_index);
        this.signature_index = signature_index;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // signature_index
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) signature_index);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeSignature(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) signature_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeSignature) {
            AttributeSignature o = (AttributeSignature) obj;
            if (
                signature_index == o.signature_index &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
