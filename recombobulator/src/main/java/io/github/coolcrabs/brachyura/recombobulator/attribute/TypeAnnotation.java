package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.ClassDecodeException;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetLocalvar.LocalvarTableEntry;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

public class TypeAnnotation {
    public Target target;
    public TypePath target_path;
    public int type_index;
    public List<ElementValuePair> pairs;

    public int read(ByteBuffer b, int pos) {
        byte target_type = b.get(pos);
        pos += 1;
        switch (target_type) {
            case 0x00:
            case 0x01:
                {
                    int type_parameter_index = u1(b, pos);
                    pos += 1;
                    target = new TargetTypeParameter(target_type, type_parameter_index);
                }
                break;
            case 0x10:
                {
                    int supertype_index = u2(b, pos);
                    pos += 2;
                    target = new TargetSupertype(target_type, supertype_index);
                }
                break;
            case 0x11:
            case 0x12:
                {
                    int type_parameter_index = u1(b, pos);
                    pos += 1;
                    int bound_index = u1(b, pos);
                    pos += 1;
                    target = new TargetTypeParameterBound(target_type, type_parameter_index, bound_index);
                }
                break;
            case 0x13:
            case 0x14:
            case 0x15:
                {
                    target = new TargetEmpty(target_type);
                }
                break;
            case 0x16:
                {
                    int formal_parameter_index = u1(b, pos);
                    pos += 1;
                    target = new TargetFormalParameter(target_type, formal_parameter_index);
                }
                break;
            case 0x17:
                {
                    int throws_type_index = u2(b, pos);
                    pos += 2;
                    target = new TargetThrows(target_type, throws_type_index);
                }
                break;
            case 0x40:
            case 0x41:
                {
                    int table_length = u2(b, pos);
                    pos += 2;
                    ArrayList<LocalvarTableEntry> table = new ArrayList<>(table_length);
                    for (int i = 0; i < table_length; i++) {
                        int start_pc = u2(b, pos);
                        pos += 2;
                        int length = u2(b, pos);
                        pos += 2;
                        int index = u2(b, pos);
                        pos += 2;
                        table.add(new LocalvarTableEntry(start_pc, length, index));
                    }
                    target = new TargetLocalvar(target_type, table);
                }
                break;
            case 0x42:
                {
                    int exception_table_index = u2(b, pos);
                    pos += 2;
                    target = new TargetCatch(target_type, exception_table_index);
                }
                break;
            case 0x43:
            case 0x44:
            case 0x45:
            case 0x46:
                {
                    int offset = u2(b, pos);
                    pos += 2;
                    target = new TargetOffset(target_type, offset);
                }
                break;
            case 0x47:
            case 0x48:
            case 0x49:
            case 0x4A:
            case 0x4B:
                {
                    int offset = u2(b, pos);
                    pos += 2;
                    int type_argument_index = u1(b, pos);
                    pos += 1;
                    target = new TargetTypeArgument(target_type, offset, type_argument_index);
                }
                break;
            default:
                throw new ClassDecodeException("Unknown target type: " + Integer.toHexString(type_index & 0xFF));
        }
        int path_length = u1(b, pos);
        pos += 1;
        ArrayList<EntryPath> path = new ArrayList<>(path_length);
        for (int i = 0; i < path_length; i++) {
            byte type_path_kind = b.get(pos);
            pos += 1;
            int type_argument_index = u1(b, pos);
            pos += 1;
            path.add(new EntryPath(type_path_kind, type_argument_index));
        }
        target_path = new TypePath(path);
        type_index = u2(b, pos);
        pos += 2;
        int num_element_value_pairs = u2(b, pos);
        pos += 2;
        pairs = new ArrayList<>(num_element_value_pairs);
        for (int i = 0; i < num_element_value_pairs; i++) {
            int element_name_index = u2(b, pos);
            pos += 2;
            ElementValue element_value = ElementValue.read(b, pos);
            pos += element_value.byteSize();
            pairs.add(new ElementValuePair(element_name_index, element_value));
        }
        return pos;
    }

    public int byteSize() {
        int size = 0;
        size += target.byteSize();
        size += target_path.byteSize();
        size += 2; // type_index
        size += 2; // num_element_value_pairs
        for (ElementValuePair p : pairs) size += p.byteSize();
        return size;
    }

    public void write(RecombobulatorOutput o) {
        target.write(o);
        target_path.write(o);
        o.writeShort((short)type_index);
        o.writeShort((short)pairs.size());
        for (ElementValuePair e : pairs) {
            e.write(o);
        }
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitTypeAnnotation(this);
        target.accept(v);
        target_path.accept(v);
        for (ElementValuePair evp : pairs) evp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + target.hashCode();
        result = 37*result + target_path.hashCode();
        result = 37*result + type_index;
        result = 37*result + pairs.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeAnnotation) {
            TypeAnnotation o = (TypeAnnotation) obj;
            return
                target.equals(o.target) &&
                target_path.equals(o.target_path) &&
                type_index == o.type_index &&
                pairs.equals(o.pairs);
        }
        return false;
    }
}
