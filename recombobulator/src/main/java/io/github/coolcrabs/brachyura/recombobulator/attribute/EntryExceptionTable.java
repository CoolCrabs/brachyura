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

public final class EntryExceptionTable {
    public static EntryExceptionTable read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int start_pc = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int end_pc = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int handler_pc = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int catch_type = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new EntryExceptionTable(
                start_pc,
                end_pc,
                handler_pc,
                catch_type
            );
    }

    public int start_pc;
    public int end_pc;
    public int handler_pc;
    public int catch_type;

    public EntryExceptionTable(int start_pc, int end_pc, int handler_pc, int catch_type) {
        this.start_pc = start_pc;
        this.end_pc = end_pc;
        this.handler_pc = handler_pc;
        this.catch_type = catch_type;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // start_pc
        size += 2; // end_pc
        size += 2; // handler_pc
        size += 2; // catch_type
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) start_pc);
        o.writeShort((short) end_pc);
        o.writeShort((short) handler_pc);
        o.writeShort((short) catch_type);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryExceptionTable(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) start_pc;
        result = 37*result + (int) end_pc;
        result = 37*result + (int) handler_pc;
        result = 37*result + (int) catch_type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryExceptionTable) {
            EntryExceptionTable o = (EntryExceptionTable) obj;
            if (
                start_pc == o.start_pc &&
                end_pc == o.end_pc &&
                handler_pc == o.handler_pc &&
                catch_type == o.catch_type &&
                true
            ) return true;
        }
        return false;
    }
}
