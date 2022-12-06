package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetOffset extends Target {
    public int offset;

    public TargetOffset(byte tag, int offset) {
        super(tag);
        this.offset = offset;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)offset);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetOffset(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + offset;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetOffset) {
            TargetOffset o = (TargetOffset) obj;
            return
                offset == o.offset &&
                tag == o.tag;
        }
        return false;
    }
}
