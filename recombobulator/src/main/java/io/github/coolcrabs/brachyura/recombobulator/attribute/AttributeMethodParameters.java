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

public final class AttributeMethodParameters extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("MethodParameters");

    public static final AttributeType TYPE = new AttributeType(52, 0, EnumSet.of(AttributeType.Location.METHOD_INFO)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int parameters_count = b.get(pos);
            pos += 1;
            List<EntryParameters> parameters = new ArrayList<>(parameters_count);
            for (int i = 0; i < parameters_count; i++) {
                EntryParameters tmp = EntryParameters.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                parameters.add(tmp);
            }
            return new AttributeMethodParameters(
                attribute_name_index,
                parameters
            );
        }
    };

    public List<EntryParameters> parameters;

    public AttributeMethodParameters(int attribute_name_index, List<EntryParameters> parameters) {
        super(attribute_name_index);
        this.parameters = parameters;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 1; // parameters_count
        for (EntryParameters tmp : parameters) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeByte((byte)parameters.size());
        for (EntryParameters tmp : parameters) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeMethodParameters(this);
        for (EntryParameters tmp : parameters) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + parameters.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeMethodParameters) {
            AttributeMethodParameters o = (AttributeMethodParameters) obj;
            if (
                parameters.equals(o.parameters) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
