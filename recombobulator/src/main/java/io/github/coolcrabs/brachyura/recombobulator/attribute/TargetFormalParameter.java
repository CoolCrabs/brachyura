package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetFormalParameter extends Target {
    public int formal_parameter_index;

    public TargetFormalParameter(byte tag, int formal_parameter_index) {
        super(tag);
        this.formal_parameter_index = formal_parameter_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 1;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeByte((byte)formal_parameter_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetFormalParameter(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + formal_parameter_index;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetFormalParameter) {
            TargetFormalParameter o = (TargetFormalParameter) obj;
            return
                formal_parameter_index == o.formal_parameter_index &&
                tag == o.tag;
        }
        return false;
    }
}
