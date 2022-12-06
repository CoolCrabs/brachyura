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

public final class EntryLineNumberTable {
    public static EntryLineNumberTable read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int start_pc = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int line_number = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new EntryLineNumberTable(
                start_pc,
                line_number
            );
    }

    public int start_pc;
    public int line_number;

    public EntryLineNumberTable(int start_pc, int line_number) {
        this.start_pc = start_pc;
        this.line_number = line_number;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // start_pc
        size += 2; // line_number
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) start_pc);
        o.writeShort((short) line_number);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryLineNumberTable(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) start_pc;
        result = 37*result + (int) line_number;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryLineNumberTable) {
            EntryLineNumberTable o = (EntryLineNumberTable) obj;
            if (
                start_pc == o.start_pc &&
                line_number == o.line_number &&
                true
            ) return true;
        }
        return false;
    }
}
