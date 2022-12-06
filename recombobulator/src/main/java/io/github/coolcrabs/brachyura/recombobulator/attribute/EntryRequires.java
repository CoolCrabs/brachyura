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

public final class EntryRequires {
    public static EntryRequires read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int requires_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int requires_flags = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int requires_version_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new EntryRequires(
                requires_index,
                requires_flags,
                requires_version_index
            );
    }

    public int requires_index;
    public int requires_flags;
    public int requires_version_index;

    public EntryRequires(int requires_index, int requires_flags, int requires_version_index) {
        this.requires_index = requires_index;
        this.requires_flags = requires_flags;
        this.requires_version_index = requires_version_index;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // requires_index
        size += 2; // requires_flags
        size += 2; // requires_version_index
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) requires_index);
        o.writeShort((short) requires_flags);
        o.writeShort((short) requires_version_index);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryRequires(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) requires_index;
        result = 37*result + (int) requires_flags;
        result = 37*result + (int) requires_version_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryRequires) {
            EntryRequires o = (EntryRequires) obj;
            if (
                requires_index == o.requires_index &&
                requires_flags == o.requires_flags &&
                requires_version_index == o.requires_version_index &&
                true
            ) return true;
        }
        return false;
    }
}
