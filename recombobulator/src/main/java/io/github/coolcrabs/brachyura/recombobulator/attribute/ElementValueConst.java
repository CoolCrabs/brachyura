package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class ElementValueConst extends ElementValue {
    public int const_value_index;

    ElementValueConst(ElementTag tag, int const_value_index) {
        super(tag);
        this.const_value_index = const_value_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)const_value_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitElementValueConst(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + tag.value;
        result = 37*result + const_value_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ElementValueConst) {
            ElementValueConst o = (ElementValueConst) obj;
            if (
                const_value_index == o.const_value_index &&
                tag == o.tag
            ) return true;
        }
        return false;
    }
}
