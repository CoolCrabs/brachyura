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

public final class AttributePermittedSubclasses extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("PermittedSubclasses");

    public static final AttributeType TYPE = new AttributeType(61, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int number_of_classes = ByteBufferUtil.u2(b, pos);
            pos += 2;
            U2Slice classes = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (number_of_classes * 2)));
            pos += number_of_classes * 2;
            return new AttributePermittedSubclasses(
                attribute_name_index,
                classes
            );
        }
    };

    public U2Slice classes;

    public AttributePermittedSubclasses(int attribute_name_index, U2Slice classes) {
        super(attribute_name_index);
        this.classes = classes;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // number_of_classes
        size += classes.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) classes.size());
        classes.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributePermittedSubclasses(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + classes.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributePermittedSubclasses) {
            AttributePermittedSubclasses o = (AttributePermittedSubclasses) obj;
            if (
                classes.equals(o.classes) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
