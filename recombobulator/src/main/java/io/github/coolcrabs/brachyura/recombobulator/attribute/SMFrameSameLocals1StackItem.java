package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class SMFrameSameLocals1StackItem extends StackMapFrame {
    public VerificationType stack;

    public SMFrameSameLocals1StackItem(int frame_type, VerificationType stack) {
        super(frame_type);
        this.stack = stack;
    }

    @Override
    int byteSize() {
        return 1 + stack.byteSize();
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        stack.write(o);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitSMFrameSameLocals1StackItem(this);
        stack.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + stack.hashCode();
        result = 37*result + frame_type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SMFrameSameLocals1StackItem) {
            SMFrameSameLocals1StackItem o = (SMFrameSameLocals1StackItem) obj;
            return
                frame_type == o.frame_type &&
                stack.equals(o.stack);
        }
        return false;
    }
}
