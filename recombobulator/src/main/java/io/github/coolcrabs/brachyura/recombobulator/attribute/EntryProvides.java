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

public final class EntryProvides {
    public static EntryProvides read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int provides_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int provides_with_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            U2Slice provides_with_index = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (provides_with_count * 2)));
            pos += provides_with_count * 2;
            return new EntryProvides(
                provides_index,
                provides_with_index
            );
    }

    public int provides_index;
    public U2Slice provides_with_index;

    public EntryProvides(int provides_index, U2Slice provides_with_index) {
        this.provides_index = provides_index;
        this.provides_with_index = provides_with_index;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // provides_index
        size += 2; // provides_with_count
        size += provides_with_index.byteSize();
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) provides_index);
        o.writeShort((short) provides_with_index.size());
        provides_with_index.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryProvides(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) provides_index;
        result = 37*result + provides_with_index.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryProvides) {
            EntryProvides o = (EntryProvides) obj;
            if (
                provides_index == o.provides_index &&
                provides_with_index.equals(o.provides_with_index) &&
                true
            ) return true;
        }
        return false;
    }
}
