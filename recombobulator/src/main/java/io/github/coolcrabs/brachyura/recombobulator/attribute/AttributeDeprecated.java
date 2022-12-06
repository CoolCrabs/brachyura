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

public final class AttributeDeprecated extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("Deprecated");

    public static final AttributeType TYPE = new AttributeType(45, 3, EnumSet.of(AttributeType.Location.CLASS_FILE, AttributeType.Location.FIELD_INFO, AttributeType.Location.METHOD_INFO)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            return new AttributeDeprecated(
                attribute_name_index
            );
        }
    };


    public AttributeDeprecated(int attribute_name_index) {
        super(attribute_name_index);
    }

    @Override
    public int byteSize() {
        int size = 0;
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeDeprecated(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeDeprecated) {
            AttributeDeprecated o = (AttributeDeprecated) obj;
            if (
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
