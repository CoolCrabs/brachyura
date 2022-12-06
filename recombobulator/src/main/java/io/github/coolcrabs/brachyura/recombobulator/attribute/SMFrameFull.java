package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class SMFrameFull extends StackMapFrame {
    public int offset_delta;
    public List<VerificationType> locals;
    public List<VerificationType> stack;

    public SMFrameFull(int frame_type, int offset_delta, List<VerificationType> locals, List<VerificationType> stack) {
        super(frame_type);
        this.offset_delta = offset_delta;
        this.locals = locals;
        this.stack = stack;
    }

    @Override
    int byteSize() {
        int size = 1;
        size += 2;
        size += 2;
        for (VerificationType vt : locals) size += vt.byteSize();
        size += 2;
        for (VerificationType vt : stack) size += vt.byteSize();
        return size;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)offset_delta);
        o.writeShort((short)locals.size());
        for (VerificationType vt : locals) vt.write(o);
        o.writeShort((short)stack.size());
        for (VerificationType vt : stack) vt.write(o);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitSMFrameFull(this);
        for (VerificationType vt : locals) vt.accept(v);
        for (VerificationType vt : stack) vt.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + offset_delta;
        result = 37*result + locals.hashCode();
        result = 37*result + stack.hashCode();
        result = 37*result + frame_type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SMFrameFull) {
            SMFrameFull o = (SMFrameFull) obj;
            return
                offset_delta == o.offset_delta &&
                locals.equals(o.locals) &&
                stack.equals(o.stack) &&
                frame_type == o.frame_type;
        }
        return false;
    }
}
