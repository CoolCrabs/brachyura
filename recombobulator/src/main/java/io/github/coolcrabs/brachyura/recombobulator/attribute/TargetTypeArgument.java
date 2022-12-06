package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetTypeArgument extends Target {
    public int offset;
    public int type_argument_index;

    public TargetTypeArgument(byte tag, int offset, int type_argument_index) {
        super(tag);
        this.offset = offset;
        this.type_argument_index = type_argument_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 3;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)offset);
        o.writeByte((byte)type_argument_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetTypeArgument(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + offset;
        result = 37*result + type_argument_index;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetTypeArgument) {
            TargetTypeArgument o = (TargetTypeArgument) obj;
            return
                offset == o.offset &&
                type_argument_index == o.type_argument_index &&
                tag == o.tag;
        }
        return false;
    }
}
