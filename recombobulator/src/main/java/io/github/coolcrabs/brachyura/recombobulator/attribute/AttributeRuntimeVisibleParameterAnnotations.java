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

public final class AttributeRuntimeVisibleParameterAnnotations extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("RuntimeVisibleParameterAnnotations");

    public static final AttributeType TYPE = new AttributeType(49, 0, EnumSet.of(AttributeType.Location.METHOD_INFO)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int num_parameters = b.get(pos);
            pos += 1;
            List<EntryParameterAnnotations> parameter_annotations = new ArrayList<>(num_parameters);
            for (int i = 0; i < num_parameters; i++) {
                EntryParameterAnnotations tmp = EntryParameterAnnotations.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                parameter_annotations.add(tmp);
            }
            return new AttributeRuntimeVisibleParameterAnnotations(
                attribute_name_index,
                parameter_annotations
            );
        }
    };

    public List<EntryParameterAnnotations> parameter_annotations;

    public AttributeRuntimeVisibleParameterAnnotations(int attribute_name_index, List<EntryParameterAnnotations> parameter_annotations) {
        super(attribute_name_index);
        this.parameter_annotations = parameter_annotations;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 1; // num_parameters
        for (EntryParameterAnnotations tmp : parameter_annotations) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeByte((byte)parameter_annotations.size());
        for (EntryParameterAnnotations tmp : parameter_annotations) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeRuntimeVisibleParameterAnnotations(this);
        for (EntryParameterAnnotations tmp : parameter_annotations) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + parameter_annotations.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeRuntimeVisibleParameterAnnotations) {
            AttributeRuntimeVisibleParameterAnnotations o = (AttributeRuntimeVisibleParameterAnnotations) obj;
            if (
                parameter_annotations.equals(o.parameter_annotations) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
