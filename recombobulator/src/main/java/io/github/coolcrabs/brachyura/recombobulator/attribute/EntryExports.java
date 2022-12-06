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

public final class EntryExports {
    public static EntryExports read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int exports_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int exports_flags = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int exports_to_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            U2Slice exports_to_index = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (exports_to_count * 2)));
            pos += exports_to_count * 2;
            return new EntryExports(
                exports_index,
                exports_flags,
                exports_to_index
            );
    }

    public int exports_index;
    public int exports_flags;
    public U2Slice exports_to_index;

    public EntryExports(int exports_index, int exports_flags, U2Slice exports_to_index) {
        this.exports_index = exports_index;
        this.exports_flags = exports_flags;
        this.exports_to_index = exports_to_index;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // exports_index
        size += 2; // exports_flags
        size += 2; // exports_to_count
        size += exports_to_index.byteSize();
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) exports_index);
        o.writeShort((short) exports_flags);
        o.writeShort((short) exports_to_index.size());
        exports_to_index.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryExports(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) exports_index;
        result = 37*result + (int) exports_flags;
        result = 37*result + exports_to_index.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryExports) {
            EntryExports o = (EntryExports) obj;
            if (
                exports_index == o.exports_index &&
                exports_flags == o.exports_flags &&
                exports_to_index.equals(o.exports_to_index) &&
                true
            ) return true;
        }
        return false;
    }
}
