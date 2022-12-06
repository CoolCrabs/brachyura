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

public final class AttributeModulePackages extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("ModulePackages");

    public static final AttributeType TYPE = new AttributeType(53, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int package_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            U2Slice package_index = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (package_count * 2)));
            pos += package_count * 2;
            return new AttributeModulePackages(
                attribute_name_index,
                package_index
            );
        }
    };

    public U2Slice package_index;

    public AttributeModulePackages(int attribute_name_index, U2Slice package_index) {
        super(attribute_name_index);
        this.package_index = package_index;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // package_count
        size += package_index.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) package_index.size());
        package_index.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeModulePackages(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + package_index.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeModulePackages) {
            AttributeModulePackages o = (AttributeModulePackages) obj;
            if (
                package_index.equals(o.package_index) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
