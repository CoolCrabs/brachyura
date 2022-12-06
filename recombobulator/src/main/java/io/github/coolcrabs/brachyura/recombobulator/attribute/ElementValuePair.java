package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class ElementValuePair {
    public int element_name_index;
    public ElementValue element_value;

    public ElementValuePair(int element_name_index, ElementValue element_value) {
        this.element_name_index = element_name_index;
        this.element_value = element_value;
    }

    public int byteSize() {
        return 2 + element_value.byteSize();
    }

    public void write(RecombobulatorOutput o) {
        o.writeShort((short)element_name_index);
        element_value.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitElementValuePair(this);
        element_value.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + element_name_index;
        result = 37*result + element_value.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ElementValuePair) {
            ElementValuePair o = (ElementValuePair) obj;
            if (
                element_name_index == o.element_name_index &&
                element_value.equals(o.element_value)
            ) return true;
        }
        return false;
    }
}