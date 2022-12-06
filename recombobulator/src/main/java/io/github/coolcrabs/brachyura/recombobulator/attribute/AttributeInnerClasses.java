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

public final class AttributeInnerClasses extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("InnerClasses");

    public static final AttributeType TYPE = new AttributeType(45, 3, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int number_of_classes = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryClasses> classes = new ArrayList<>(number_of_classes);
            for (int i = 0; i < number_of_classes; i++) {
                EntryClasses tmp = EntryClasses.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                classes.add(tmp);
            }
            return new AttributeInnerClasses(
                attribute_name_index,
                classes
            );
        }
    };

    public List<EntryClasses> classes;

    public AttributeInnerClasses(int attribute_name_index, List<EntryClasses> classes) {
        super(attribute_name_index);
        this.classes = classes;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // number_of_classes
        for (EntryClasses tmp : classes) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) classes.size());
        for (EntryClasses tmp : classes) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeInnerClasses(this);
        for (EntryClasses tmp : classes) tmp.accept(v);
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
        if (obj instanceof AttributeInnerClasses) {
            AttributeInnerClasses o = (AttributeInnerClasses) obj;
            if (
                classes.equals(o.classes) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
