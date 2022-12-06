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

public final class AttributeModuleMainClass extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("ModuleMainClass");

    public static final AttributeType TYPE = new AttributeType(53, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int main_class_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new AttributeModuleMainClass(
                attribute_name_index,
                main_class_index
            );
        }
    };

    public int main_class_index;

    public AttributeModuleMainClass(int attribute_name_index, int main_class_index) {
        super(attribute_name_index);
        this.main_class_index = main_class_index;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // main_class_index
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) main_class_index);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeModuleMainClass(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) main_class_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeModuleMainClass) {
            AttributeModuleMainClass o = (AttributeModuleMainClass) obj;
            if (
                main_class_index == o.main_class_index &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
