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

public final class AttributeSourceDebugExtension extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("SourceDebugExtension");

    public static final AttributeType TYPE = new AttributeType(49, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            ByteBuffer debug_extension = ByteBufferUtil.slice(b, pos, pos + attribute_length);
            pos += attribute_length;
            return new AttributeSourceDebugExtension(
                attribute_name_index,
                debug_extension
            );
        }
    };

    public ByteBuffer debug_extension;

    public AttributeSourceDebugExtension(int attribute_name_index, ByteBuffer debug_extension) {
        super(attribute_name_index);
        this.debug_extension = debug_extension;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += debug_extension.remaining();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeInt((int) debug_extension.remaining());
        o.writeBytes(debug_extension);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeSourceDebugExtension(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + debug_extension.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeSourceDebugExtension) {
            AttributeSourceDebugExtension o = (AttributeSourceDebugExtension) obj;
            if (
                debug_extension.equals(o.debug_extension) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
