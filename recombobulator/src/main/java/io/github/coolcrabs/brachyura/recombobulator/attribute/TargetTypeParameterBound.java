package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetTypeParameterBound extends Target {
    public int type_parameter_index;
    public int bound_index;

    public TargetTypeParameterBound(byte tag, int type_parameter_index, int bound_index) {
        super(tag);
        this.type_parameter_index = type_parameter_index;
        this.bound_index = bound_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeByte((byte)type_parameter_index);
        o.writeByte((byte)bound_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetTypeParameterBound(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + type_parameter_index;
        result = 37*result + bound_index;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetTypeParameterBound) {
            TargetTypeParameterBound o = (TargetTypeParameterBound) obj;
            return
                type_parameter_index == o.type_parameter_index &&
                bound_index == o.bound_index &&
                tag == o.tag;
        }
        return false;
    }
}
