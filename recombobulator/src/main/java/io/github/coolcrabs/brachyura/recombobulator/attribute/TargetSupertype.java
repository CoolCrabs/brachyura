package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetSupertype extends Target {
    public int supertype_index;

    public TargetSupertype(byte tag, int supertype_index) {
        super(tag);
        this.supertype_index = supertype_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)supertype_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetSupertype(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + supertype_index;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetSupertype) {
            TargetSupertype o = (TargetSupertype) obj;
            return
                supertype_index == o.supertype_index &&
                tag == o.tag;
        }
        return false;
    }
}
