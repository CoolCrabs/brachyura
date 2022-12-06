package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.util.Arrays;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class SMFrameAppend extends StackMapFrame {
    public int offset_delta;
    public VerificationType[] locals;

    public SMFrameAppend(int frame_type, int offset_delta, VerificationType[] locals) {
        super(frame_type);
        this.offset_delta = offset_delta;
        this.locals = locals;
    }

    @Override
    int byteSize() {
        int size = 3;
        for (VerificationType vt : locals) size += vt.byteSize();
        return size;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)offset_delta);
        for (VerificationType vt : locals) {
            vt.write(o);
        }
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitSMFrameAppend(this);
        for (VerificationType vt : locals) vt.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + offset_delta;
        result = 37*result + Arrays.hashCode(locals);
        result = 37*result + frame_type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SMFrameAppend) {
            SMFrameAppend o = (SMFrameAppend) obj;
            return
                offset_delta == o.offset_delta &&
                Arrays.equals(locals, o.locals) &&
                frame_type == o.frame_type;
        }
        return false;
    }
}
