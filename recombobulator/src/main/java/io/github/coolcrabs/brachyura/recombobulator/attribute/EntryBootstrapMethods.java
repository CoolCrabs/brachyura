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

public final class EntryBootstrapMethods {
    public static EntryBootstrapMethods read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int bootstrap_method_ref = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int num_bootstrap_arguments = ByteBufferUtil.u2(b, pos);
            pos += 2;
            U2Slice bootstrap_arguments = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (num_bootstrap_arguments * 2)));
            pos += num_bootstrap_arguments * 2;
            return new EntryBootstrapMethods(
                bootstrap_method_ref,
                bootstrap_arguments
            );
    }

    public int bootstrap_method_ref;
    public U2Slice bootstrap_arguments;

    public EntryBootstrapMethods(int bootstrap_method_ref, U2Slice bootstrap_arguments) {
        this.bootstrap_method_ref = bootstrap_method_ref;
        this.bootstrap_arguments = bootstrap_arguments;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // bootstrap_method_ref
        size += 2; // num_bootstrap_arguments
        size += bootstrap_arguments.byteSize();
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) bootstrap_method_ref);
        o.writeShort((short) bootstrap_arguments.size());
        bootstrap_arguments.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryBootstrapMethods(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) bootstrap_method_ref;
        result = 37*result + bootstrap_arguments.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryBootstrapMethods) {
            EntryBootstrapMethods o = (EntryBootstrapMethods) obj;
            if (
                bootstrap_method_ref == o.bootstrap_method_ref &&
                bootstrap_arguments.equals(o.bootstrap_arguments) &&
                true
            ) return true;
        }
        return false;
    }
}
