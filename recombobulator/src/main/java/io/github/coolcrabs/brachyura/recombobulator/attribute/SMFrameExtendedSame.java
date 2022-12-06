package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class SMFrameExtendedSame extends StackMapFrame {
    public int offset_delta;

    public SMFrameExtendedSame(int frame_type, int offset_delta) {
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
        v.visitSMFrameExtendedSame(this);
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
        if (obj instanceof SMFrameExtendedSame) {
            SMFrameExtendedSame o = (SMFrameExtendedSame) obj;
            return
                offset_delta == o.offset_delta &&
                frame_type == o.frame_type;
        }
        return false;
    }
}
