package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class ElementValueClass extends ElementValue {
    public int class_info_index;

    public ElementValueClass(int class_info_index) {
        super(ElementTag.CLASS);
        this.class_info_index = class_info_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 2;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)class_info_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitElementValueClass(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + tag.value;
        result = 37*result + class_info_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ElementValueClass) {
            ElementValueClass o = (ElementValueClass) obj;
            if (
                class_info_index == o.class_info_index &&
                tag == o.tag
            ) return true;
        }
        return false;
    }
}
