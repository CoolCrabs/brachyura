package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetCatch extends Target {
    public int exception_table_index;

    public TargetCatch(byte tag, int exception_table_index) {
        super(tag);
        this.exception_table_index = exception_table_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)exception_table_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetCatch(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + exception_table_index;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetCatch) {
            TargetCatch o = (TargetCatch) obj;
            return
                exception_table_index == o.exception_table_index &&
                tag == o.tag;
        }
        return false;
    }
}
