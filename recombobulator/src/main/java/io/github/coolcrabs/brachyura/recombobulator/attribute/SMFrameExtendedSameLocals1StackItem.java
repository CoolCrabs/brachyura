package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class SMFrameExtendedSameLocals1StackItem extends StackMapFrame {
    public int offset_delta;
    public VerificationType stack;

    public SMFrameExtendedSameLocals1StackItem(int frame_type, int offset_delta, VerificationType stack) {
        super(frame_type);
        this.offset_delta = offset_delta;
        this.stack = stack;
    }

    @Override
    int byteSize() {
        return 3 + stack.byteSize();
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)offset_delta);
        stack.write(o);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitSMFrameExtendedSameLocals1StackItem(this);
        stack.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + offset_delta;
        result = 37*result + stack.hashCode();
        result = 37*result + frame_type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SMFrameExtendedSameLocals1StackItem) {
            SMFrameExtendedSameLocals1StackItem o = (SMFrameExtendedSameLocals1StackItem) obj;
            return
                offset_delta == o.offset_delta &&
                stack.equals(o.stack) &&
                frame_type == o.frame_type;
        }
        return false;
    }
}
