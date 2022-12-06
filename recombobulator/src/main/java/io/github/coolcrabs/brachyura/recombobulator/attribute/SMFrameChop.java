package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class SMFrameChop extends StackMapFrame {
    public int offset_delta;

    public SMFrameChop(int frame_type, int offset_delta) {
        super(frame_type);
        this.offset_delta = offset_delta;
    }

    @Override
    int byteSize() {
        return 3;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)offset_delta);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitSMFrameChop(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + offset_delta;
        result = 37*result + frame_type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SMFrameChop) {
            SMFrameChop o = (SMFrameChop) obj;
            return
                offset_delta == o.offset_delta &&
                frame_type == o.frame_type;
        }
        return false;
    }
}
