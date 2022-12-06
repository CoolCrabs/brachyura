package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class SMFrameSame extends StackMapFrame {
    public SMFrameSame(int frame_type) {
        super(frame_type);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitSMFrameSame(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + frame_type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SMFrameSame) {
            SMFrameSame o = (SMFrameSame) obj;
            return frame_type == o.frame_type;
        }
        return false;
    }
}
