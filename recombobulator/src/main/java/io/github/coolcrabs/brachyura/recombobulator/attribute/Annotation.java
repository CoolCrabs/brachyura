package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

public class Annotation {
    public int type_index;
    public List<ElementValuePair> element_value_pairs;

    public Annotation(int type_index, List<ElementValuePair> element_value_pairs) {
        this.type_index = type_index;
        this.element_value_pairs = element_value_pairs;
    }

    public static Annotation read(ByteBuffer b, int pos) {
        int type_index = u2(b, pos);
        pos += 2;
        int num_element_value_pairs = u2(b, pos);
        pos += 2;
        ArrayList<ElementValuePair> element_value_pairs = new ArrayList<>(num_element_value_pairs);
        for (int i = 0; i < num_element_value_pairs; i++) {
            int element_name_index = u2(b, pos);
            pos += 2;
            ElementValue element_value = ElementValue.read(b, pos);
            pos += element_value.byteSize();
            element_value_pairs.add(new ElementValuePair(element_name_index, element_value));
        }
        return new Annotation(type_index, element_value_pairs);
    }    

    int byteSize() {
        int size = 4;
        for (ElementValuePair v : element_value_pairs) size += v.byteSize();
        return size;
    }

    void write(RecombobulatorOutput o) {
        o.writeShort((short)type_index);
        o.writeShort((short)element_value_pairs.size());
        for (ElementValuePair v : element_value_pairs) v.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAnnotation(this);
        for (ElementValuePair evp : element_value_pairs) evp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + type_index;
        result = 37*result + element_value_pairs.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Annotation) {
            Annotation o = (Annotation) obj;
            if (
                type_index == o.type_index &&
                element_value_pairs.equals(o.element_value_pairs)
            ) return true;
        }
        return false;
    }
}
