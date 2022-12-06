package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public final class AttributeUnknown extends Attribute {
    public static final AttributeType TYPE = new AttributeType(0, 0, EnumSet.allOf(AttributeType.Location.class)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            return new AttributeUnknown(attribute_name_index, ByteBufferUtil.slice(b, pos, pos + attribute_length));
        }
    }; 

    public final ByteBuffer b;

    AttributeUnknown(int attribute_name_index, ByteBuffer b) {
        super(attribute_name_index);
        this.b = b;
    }

    @Override
    public int byteSize() {
        return b.remaining();
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeBytes(b);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeUnknown(this);
    }

    @Override
    public int hashCode() {
        return b.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeUnknown) {
            AttributeUnknown o = (AttributeUnknown) obj;
            return b.equals(o.b) && attribute_name_index == o.attribute_name_index;
        }
        return false;
    }
}
