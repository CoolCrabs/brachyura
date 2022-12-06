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

public final class AttributeBootstrapMethods extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("BootstrapMethods");

    public static final AttributeType TYPE = new AttributeType(51, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int num_bootstrap_methods = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryBootstrapMethods> bootstrap_methods = new ArrayList<>(num_bootstrap_methods);
            for (int i = 0; i < num_bootstrap_methods; i++) {
                EntryBootstrapMethods tmp = EntryBootstrapMethods.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                bootstrap_methods.add(tmp);
            }
            return new AttributeBootstrapMethods(
                attribute_name_index,
                bootstrap_methods
            );
        }
    };

    public List<EntryBootstrapMethods> bootstrap_methods;

    public AttributeBootstrapMethods(int attribute_name_index, List<EntryBootstrapMethods> bootstrap_methods) {
        super(attribute_name_index);
        this.bootstrap_methods = bootstrap_methods;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // num_bootstrap_methods
        for (EntryBootstrapMethods tmp : bootstrap_methods) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) bootstrap_methods.size());
        for (EntryBootstrapMethods tmp : bootstrap_methods) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeBootstrapMethods(this);
        for (EntryBootstrapMethods tmp : bootstrap_methods) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + bootstrap_methods.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeBootstrapMethods) {
            AttributeBootstrapMethods o = (AttributeBootstrapMethods) obj;
            if (
                bootstrap_methods.equals(o.bootstrap_methods) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
