package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetTypeParameter extends Target {
    public int type_parameter_index;

    TargetTypeParameter(byte tag, int type_parameter_index) {
        super(tag);
        this.type_parameter_index = type_parameter_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 1;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeByte((byte) type_parameter_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetTypeParameter(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + type_parameter_index;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetTypeParameter) {
            TargetTypeParameter o = (TargetTypeParameter) obj;
            return
                type_parameter_index == o.type_parameter_index &&
                tag == o.tag;
        }
        return false;
    }
    
}
