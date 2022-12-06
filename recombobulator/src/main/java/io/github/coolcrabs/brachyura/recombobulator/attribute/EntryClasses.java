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

public final class EntryClasses {
    public static EntryClasses read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int inner_class_info_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int outer_class_info_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int inner_name_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int inner_class_access_flags = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new EntryClasses(
                inner_class_info_index,
                outer_class_info_index,
                inner_name_index,
                inner_class_access_flags
            );
    }

    public int inner_class_info_index;
    public int outer_class_info_index;
    public int inner_name_index;
    public int inner_class_access_flags;

    public EntryClasses(int inner_class_info_index, int outer_class_info_index, int inner_name_index, int inner_class_access_flags) {
        this.inner_class_info_index = inner_class_info_index;
        this.outer_class_info_index = outer_class_info_index;
        this.inner_name_index = inner_name_index;
        this.inner_class_access_flags = inner_class_access_flags;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // inner_class_info_index
        size += 2; // outer_class_info_index
        size += 2; // inner_name_index
        size += 2; // inner_class_access_flags
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) inner_class_info_index);
        o.writeShort((short) outer_class_info_index);
        o.writeShort((short) inner_name_index);
        o.writeShort((short) inner_class_access_flags);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryClasses(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) inner_class_info_index;
        result = 37*result + (int) outer_class_info_index;
        result = 37*result + (int) inner_name_index;
        result = 37*result + (int) inner_class_access_flags;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryClasses) {
            EntryClasses o = (EntryClasses) obj;
            if (
                inner_class_info_index == o.inner_class_info_index &&
                outer_class_info_index == o.outer_class_info_index &&
                inner_name_index == o.inner_name_index &&
                inner_class_access_flags == o.inner_class_access_flags &&
                true
            ) return true;
        }
        return false;
    }
}
