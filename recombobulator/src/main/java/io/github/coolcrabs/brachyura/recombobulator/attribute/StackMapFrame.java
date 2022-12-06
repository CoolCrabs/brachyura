package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

public abstract class StackMapFrame {
    public int frame_type;
    
    public StackMapFrame(int frame_type) {
        this.frame_type = frame_type;
    }

    public static int read(ByteBuffer b, int pos, int number_of_entries, List<StackMapFrame> entries) {
        for (int i = 0; i < number_of_entries; i++) {
            int frame_type = u1(b, pos);
            pos += 1;
            if (frame_type <= 63) {
                entries.add(new SMFrameSame(frame_type));
            } else if (frame_type <= 127) {
                VerificationType stack = VerificationType.read(b, pos);
                pos += stack.byteSize();
                entries.add(new SMFrameSameLocals1StackItem(frame_type, stack));
            } else if (frame_type == 247) {
                int offset_delta = u2(b, pos);
                pos += 2;
                VerificationType stack = VerificationType.read(b, pos);
                pos += stack.byteSize();
                entries.add(new SMFrameExtendedSameLocals1StackItem(frame_type, offset_delta, stack));
            } else if (frame_type >= 248 && frame_type <= 250) {
                int offset_delta = u2(b, pos);
                pos += 2;
                entries.add(new SMFrameChop(frame_type, offset_delta));
            } else if (frame_type == 251) {
                int offset_delta = u2(b, pos);
                pos += 2;
                entries.add(new SMFrameExtendedSame(frame_type, offset_delta));
            } else if (frame_type >= 252 && frame_type <= 254) {
                int offset_delta = u2(b, pos);
                pos += 2;
                int localsCount = frame_type - 251;
                VerificationType[] locals = new VerificationType[localsCount];
                for (int j = 0; j < localsCount; j++) {
                    VerificationType vt = VerificationType.read(b, pos);
                    pos += vt.byteSize(); 
                    locals[j] = vt;
                }
                entries.add(new SMFrameAppend(frame_type, offset_delta, locals));
            } else if (frame_type == 255) {
                int offset_delta = u2(b, pos);
                pos += 2;
                int number_of_locals = u2(b, pos);
                pos += 2;
                ArrayList<VerificationType> locals = new ArrayList<>();
                for (int j = 0; j < number_of_locals; j++) {
                    VerificationType t = VerificationType.read(b, pos);
                    pos += t.byteSize();
                    locals.add(t);
                }
                int number_of_stack_items = u2(b, pos);
                pos += 2;
                ArrayList<VerificationType> stack = new ArrayList<>();
                for (int j = 0; j < number_of_stack_items; j++) {
                    VerificationType t = VerificationType.read(b, pos);
                    pos += t.byteSize();
                    stack.add(t);
                }
                entries.add(new SMFrameFull(frame_type, offset_delta, locals, stack));
            }
        }
        return pos;
    }

    int byteSize() {
        return 1;
    }

    void write(RecombobulatorOutput o) {
        o.writeByte((byte)frame_type);
    }

    public abstract void accept(RecombobulatorVisitor v);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
