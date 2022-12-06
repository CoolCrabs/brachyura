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

public final class EntryOpens {
    public static EntryOpens read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int opens_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int opens_flags = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int opens_to_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            U2Slice opens_to_index = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (opens_to_count * 2)));
            pos += opens_to_count * 2;
            return new EntryOpens(
                opens_index,
                opens_flags,
                opens_to_index
            );
    }

    public int opens_index;
    public int opens_flags;
    public U2Slice opens_to_index;

    public EntryOpens(int opens_index, int opens_flags, U2Slice opens_to_index) {
        this.opens_index = opens_index;
        this.opens_flags = opens_flags;
        this.opens_to_index = opens_to_index;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // opens_index
        size += 2; // opens_flags
        size += 2; // opens_to_count
        size += opens_to_index.byteSize();
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) opens_index);
        o.writeShort((short) opens_flags);
        o.writeShort((short) opens_to_index.size());
        opens_to_index.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryOpens(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) opens_index;
        result = 37*result + (int) opens_flags;
        result = 37*result + opens_to_index.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryOpens) {
            EntryOpens o = (EntryOpens) obj;
            if (
                opens_index == o.opens_index &&
                opens_flags == o.opens_flags &&
                opens_to_index.equals(o.opens_to_index) &&
                true
            ) return true;
        }
        return false;
    }
}
