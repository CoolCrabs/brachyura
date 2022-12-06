package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class ElementValueEnum extends ElementValue {
    public int type_name_index;
    public int const_name_index;

    public ElementValueEnum(int type_name_index, int const_name_index) {
        super(ElementTag.ENUM_CLASS);
        this.type_name_index = type_name_index;
        this.const_name_index = const_name_index;
    }

    @Override
    int byteSize() {
        return super.byteSize() + 4;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)type_name_index);
        o.writeShort((short)const_name_index);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitElementValueEnum(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + tag.value;
        result = 37*result + type_name_index;
        result = 37*result + const_name_index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ElementValueEnum) {
            ElementValueEnum o = (ElementValueEnum) obj;
            if (
                type_name_index == o.type_name_index &&
                const_name_index == o.const_name_index &&
                tag == o.tag
            ) return true;
        }
        return false;
    }
}