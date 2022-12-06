package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import io.github.coolcrabs.brachyura.recombobulator.ClassDecodeException;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

public abstract class ElementValue {
    public enum ElementTag {
        BYTE('B'),
        CHAR('C'),
        DOUBLE('D'),
        FLOAT('F'),
        INT('I'),
        LONG('J'),
        SHORT('S'),
        BOOLEAN('Z'),
        STRING('s'),
        ENUM_CLASS('e'),
        CLASS('c'),
        ANNOTATION_INTERFACE('@'),
        ARRAY_TYPE('[');

        public final byte value;

        ElementTag(char value) {
            this.value = (byte) value;
        }
    }

    public final ElementTag tag;

    ElementValue(ElementTag tag) {
        this.tag = tag;
    }

    public static ElementValue read(ByteBuffer b, int pos) {
        byte tag = b.get(pos);
        pos += 1;
        switch (tag) {
            case 'B':
                return new ElementValueConst(ElementTag.BYTE, u2(b, pos));
            case 'C':
                return new ElementValueConst(ElementTag.CHAR, u2(b, pos));
            case 'D':
                return new ElementValueConst(ElementTag.DOUBLE, u2(b, pos));
            case 'F':
                return new ElementValueConst(ElementTag.FLOAT, u2(b, pos));
            case 'I':
                return new ElementValueConst(ElementTag.INT, u2(b, pos));
            case 'J':
                return new ElementValueConst(ElementTag.LONG, u2(b, pos));
            case 'S':
                return new ElementValueConst(ElementTag.SHORT, u2(b, pos));
            case 'Z':
                return new ElementValueConst(ElementTag.BOOLEAN, u2(b, pos));
            case 's':
                return new ElementValueConst(ElementTag.STRING, u2(b, pos));
            case 'e':
                return new ElementValueEnum(u2(b, pos), u2(b, pos + 2));
            case 'c':
                return new ElementValueClass(u2(b, pos));
            case '@':
                return new ElementValueAnnotation(Annotation.read(b, pos));
            case '[':
                int num_values = u2(b, pos);
                pos += 2;
                ArrayList<ElementValue> values = new ArrayList<>(num_values);
                for (int i = 0; i < num_values; i++) {
                    ElementValue ev = ElementValue.read(b, pos);
                    pos += ev.byteSize(); 
                    values.add(ev);
                }
                return new ElementValueArray(values);
            default:
                throw new ClassDecodeException("Unknown element value tag: " + Integer.toHexString(tag & 0xFF));
        }
    }

    int byteSize() {
        return 1;
    }

    void write(RecombobulatorOutput o) {
        o.writeByte(tag.value);
    }

    public abstract void accept(RecombobulatorVisitor v);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
