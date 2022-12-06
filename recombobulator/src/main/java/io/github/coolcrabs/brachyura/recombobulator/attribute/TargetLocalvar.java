package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetLocalvar extends Target {
    public List<LocalvarTableEntry> table;

    public TargetLocalvar(byte tag, List<LocalvarTableEntry> table) {
        super(tag);
        this.table = table;
    }

    @Override
    int byteSize() {
        return 3 + (6 * table.size());
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)table.size());
        for (LocalvarTableEntry lv : table) {
            o.writeShort((short)lv.start_pc);
            o.writeShort((short)lv.length);
            o.writeShort((short)lv.index);
        }
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetLocalvar(this);
        for (LocalvarTableEntry lte : table) lte.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (LocalvarTableEntry lv : table) {
            result = 37*result + lv.hashCode();
        }
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetLocalvar) {
            TargetLocalvar o = (TargetLocalvar) obj;
            return
                table.equals(o.table) &&
                tag == o.tag;
        }
        return false;
    }
    
    public static class LocalvarTableEntry {
        public int start_pc;
        public int length;
        public int index;

        public LocalvarTableEntry (int start_pc, int length, int index) {
            this.start_pc = start_pc;
            this.length = length;
            this.index = index;
        }

        public void accept(RecombobulatorVisitor v) {
            v.visitLocalvarTableEntry(this);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37*result + start_pc;
            result = 37*result + length;
            result = 37*result + index;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LocalvarTableEntry) {
                LocalvarTableEntry o = (LocalvarTableEntry) obj;
                return
                    start_pc == o.start_pc &&
                    length == o.length &&
                    index == o.index;
            }
            return super.equals(obj);
        }
    }
}
