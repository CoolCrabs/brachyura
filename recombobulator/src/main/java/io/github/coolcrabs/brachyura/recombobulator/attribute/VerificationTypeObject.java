package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class VerificationTypeObject extends VerificationType {
    public int cpool_index;

    public VerificationTypeObject(int cpool_index) {
        super((byte) 7);
        this.cpool_index = cpool_index;
    }

    @Override
    public int byteSize() {
        return 3;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)cpool_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitVerificationTypeObject(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + cpool_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VerificationTypeObject) {
            VerificationTypeObject o = (VerificationTypeObject) obj;
            return cpool_index == o.cpool_index; 
        }
        return false;
    }
}
