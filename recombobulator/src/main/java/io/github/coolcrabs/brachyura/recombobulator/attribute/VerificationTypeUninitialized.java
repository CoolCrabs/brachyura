package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class VerificationTypeUninitialized extends VerificationType {
    public int offset;

    public VerificationTypeUninitialized(int offset) {
        super((byte) 8);
        this.offset = offset;
    }

    @Override
    public int byteSize() {
        return 3;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)offset);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitVerificationTypeUninitialized(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + offset;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VerificationTypeUninitialized) {
            VerificationTypeUninitialized o = (VerificationTypeUninitialized) obj;
            return offset == o.offset;
        }
        return false;
    }
}
