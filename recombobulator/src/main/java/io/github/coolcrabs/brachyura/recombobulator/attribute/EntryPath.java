package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class EntryPath {
    public byte type_path_kind;
    public int type_argument_index;

    public EntryPath(byte type_path_kind, int type_argument_index) {
        this.type_path_kind = type_path_kind;
        this.type_argument_index = type_argument_index;
    }

    public int byteSize() {
        int result = 17;
        result = 37*result + type_path_kind;
        result = 37*result + type_argument_index;
        return result; 
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitEntryPath(this);
    }

    public void write(RecombobulatorOutput o) {
        o.writeByte(type_path_kind);
        o.writeByte((byte)type_argument_index);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + type_path_kind;
        result = 37*result + type_argument_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntryPath) {
            EntryPath o = (EntryPath) obj;
            return
                type_path_kind == o.type_path_kind &&
                type_argument_index == o.type_argument_index;
        }
        return false;
    }
}
