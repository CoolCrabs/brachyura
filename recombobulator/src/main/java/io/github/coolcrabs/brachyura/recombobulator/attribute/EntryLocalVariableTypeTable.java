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

public final class EntryLocalVariableTypeTable {
    public static EntryLocalVariableTypeTable read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int start_pc = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int length = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int name_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int signature_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            return new EntryLocalVariableTypeTable(
                start_pc,
                length,
                name_index,
                signature_index,
                index
            );
    }

    public int start_pc;
    public int length;
    public int name_index;
    public int signature_index;
    public int index;

    public EntryLocalVariableTypeTable(int start_pc, int length, int name_index, int signature_index, int index) {
        this.start_pc = start_pc;
        this.length = length;
        this.name_index = name_index;
        this.signature_index = signature_index;
        this.index = index;
    }

    public int byteSize() {
        int size = 0;
        size += 2; // start_pc
        size += 2; // length
        size += 2; // name_index
        size += 2; // signature_index
        size += 2; // index
        return size;
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short) start_pc);
        o.writeShort((short) length);
        o.writeShort((short) name_index);
        o.writeShort((short) signature_index);
        o.writeShort((short) index);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryLocalVariableTypeTable(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) start_pc;
        result = 37*result + (int) length;
        result = 37*result + (int) name_index;
        result = 37*result + (int) signature_index;
        result = 37*result + (int) index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof EntryLocalVariableTypeTable) {
            EntryLocalVariableTypeTable o = (EntryLocalVariableTypeTable) obj;
            if (
                start_pc == o.start_pc &&
                length == o.length &&
                name_index == o.name_index &&
                signature_index == o.signature_index &&
                index == o.index &&
                true
            ) return true;
        }
        return false;
    }
}
