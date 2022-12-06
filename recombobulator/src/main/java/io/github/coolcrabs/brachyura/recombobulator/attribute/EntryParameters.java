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

public final class EntryParameters {
    public static EntryParameters read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int name_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int access_flags = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new EntryParameters(
                name_index,
                access_flags
            );
    }

    public int name_index;
    public int access_flags;

    public EntryParameters(int name_index, int access_flags) {
        this.name_index = name_index;
        this.access_flags = access_flags;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // name_index
        size += 2; // access_flags
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) name_index);
        o.writeShort((short) access_flags);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryParameters(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) name_index;
        result = 37*result + (int) access_flags;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryParameters) {
            EntryParameters o = (EntryParameters) obj;
            if (
                name_index == o.name_index &&
                access_flags == o.access_flags &&
                true
            ) return true;
        }
        return false;
    }
}
