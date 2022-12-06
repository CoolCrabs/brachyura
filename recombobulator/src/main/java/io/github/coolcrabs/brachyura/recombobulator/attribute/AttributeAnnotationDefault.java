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

public final class AttributeAnnotationDefault extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("AnnotationDefault");

    public static final AttributeType TYPE = new AttributeType(49, 0, EnumSet.of(AttributeType.Location.METHOD_INFO)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            ElementValue default_value = ElementValue.read(b, pos);
            pos += default_value.byteSize();
            return new AttributeAnnotationDefault(
                attribute_name_index,
                default_value
            );
        }
    };

    public ElementValue default_value;

    public AttributeAnnotationDefault(int attribute_name_index, ElementValue default_value) {
        super(attribute_name_index);
        this.default_value = default_value;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += default_value.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        default_value.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeAnnotationDefault(this);
        default_value.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + default_value.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeAnnotationDefault) {
            AttributeAnnotationDefault o = (AttributeAnnotationDefault) obj;
            if (
                default_value.equals(o.default_value) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
