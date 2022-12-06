package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class ElementValueArray extends ElementValue {
    public List<ElementValue> values;

    public ElementValueArray(List<ElementValue> values) {
        super(ElementTag.ARRAY_TYPE);
        this.values = values;
    }

    @Override
    int byteSize() {
        int size = 1 + 2;
        for (ElementValue v : values) size += v.byteSize();
        return size;
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        o.writeShort((short)values.size());
        for (ElementValue v : values) {
            v.write(o);
        }
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitElementValueArray(this);
        for (ElementValue ev : values) ev.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + tag.value;
        result = 37*result + values.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ElementValueArray) {
            ElementValueArray o = (ElementValueArray) obj;
            if (
                values.equals(o.values) &&
                tag == o.tag
            ) return true;
        }
        return false;
    }
}