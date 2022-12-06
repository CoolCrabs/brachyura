package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetThrows extends Target {
    public int throws_type_index;

    public TargetThrows(byte tag, int throws_type_index) {
        super(tag);
        this.throws_type_index = throws_type_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)throws_type_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetThrows(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + throws_type_index;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetThrows) {
            TargetThrows o = (TargetThrows) obj;
            return
                throws_type_index == o.throws_type_index &&
                tag == o.tag;
        }
        return false;
    }
}
