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

public final class AttributeRuntimeVisibleAnnotations extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("RuntimeVisibleAnnotations");

    public static final AttributeType TYPE = new AttributeType(49, 0, EnumSet.of(AttributeType.Location.CLASS_FILE, AttributeType.Location.FIELD_INFO, AttributeType.Location.METHOD_INFO, AttributeType.Location.RECORD_COMPONENT)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int num_annotations = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<Annotation> annotations = new ArrayList<>(num_annotations);
            for (int i = 0; i < num_annotations; i++) {
                Annotation tmp = Annotation.read(b, pos);
                pos += tmp.byteSize();
                annotations.add(tmp);
            }
            return new AttributeRuntimeVisibleAnnotations(
                attribute_name_index,
                annotations
            );
        }
    };

    public List<Annotation> annotations;

    public AttributeRuntimeVisibleAnnotations(int attribute_name_index, List<Annotation> annotations) {
        super(attribute_name_index);
        this.annotations = annotations;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // num_annotations
        for (Annotation tmp : annotations) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) annotations.size());
        for (Annotation tmp : annotations) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeRuntimeVisibleAnnotations(this);
        for (Annotation tmp : annotations) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + annotations.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeRuntimeVisibleAnnotations) {
            AttributeRuntimeVisibleAnnotations o = (AttributeRuntimeVisibleAnnotations) obj;
            if (
                annotations.equals(o.annotations) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
