package io.github.coolcrabs.brachyura.recombobulator.remapper;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;

public interface Mappings {
    Mutf8Slice mapClass(Mutf8Slice srcCls);

    default NameDescPair mapField(Mutf8Slice srcCls, NameDescPair srcField) {
        return new NameDescPair(srcField.name, remapFieldDescriptor(this, srcField.desc));
    }

    default NameDescPair mapMethod(Mutf8Slice srcCls, NameDescPair srcMethod) {
        return new NameDescPair(srcMethod.name, remapMethodDescriptor(this, srcMethod.desc));
    }

    default @Nullable Mutf8Slice mapParam(Mutf8Slice srcCls, NameDescPair srcMethod, int lvtIndex) {
        return null;
    }

    public static Mutf8Slice remapFieldDescriptor(Mappings mappings, Mutf8Slice desc) {
        ByteBuffer b = desc.b;
        int start = b.position();
        int end = b.limit();
        if (end - start <= 1) return desc; // BaseType
        if (b.get(end - 1) != ';') return desc; // Array of basic type
        int arraySize = 0;
        for (int i = start; i < end; i++) {
            byte character = b.get(i);
            switch (character) {
                case '[':
                    arraySize += 1;
                    break;
                case 'L':
                    Mutf8Slice clsName = new Mutf8Slice(ByteBufferUtil.slice(b, i + 1, end - 1));
                    Mutf8Slice remapped = mappings.mapClass(clsName);
                    if (remapped.equals(clsName)) {
                        return desc;
                    }
                    byte[] r = new byte[arraySize + 2 + remapped.b.remaining()];
                    int rindex = 0;
                    for (; rindex < arraySize; rindex++) {
                        r[rindex] = '[';
                    }
                    r[rindex++] = 'L';
                    ByteBuffer rb = remapped.b;
                    int remappedend = rb.limit();
                    for (int j = remapped.b.position(); j < remappedend; j++) {
                        r[rindex++] = rb.get(j);
                    }
                    r[rindex] = ';';
                    return new Mutf8Slice(ByteBuffer.wrap(r));
                default:
                    throw new RuntimeException("Illegal desc: " + desc.toString());
            }
        }
        throw new RuntimeException("Illegal desc: " + desc.toString());
    }

    public static Mutf8Slice remapMethodDescriptor(Mappings mappings, Mutf8Slice desc) {
        ByteBuffer b = desc.b;
        int start = b.position();
        int end = b.limit();
        int size = 0;
        boolean needsRemap = false;
        for (int i = start; i < end; i++) {
            byte character = b.get(i);
            switch (character) {
                case '(':
                case ')':
                case 'V':
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                case '[':
                    size += 1;
                    break;
                case 'L':
                    int classEnd = -1;
                    for (int j = i + 1; j < end; j++) {
                        if (b.get(j) == ';') {
                            classEnd = j;
                            break;
                        }
                    }
                    Mutf8Slice oldClsName = new Mutf8Slice(ByteBufferUtil.slice(b, i + 1, classEnd));
                    i = classEnd;
                    Mutf8Slice clsName = mappings.mapClass(oldClsName);
                    if (!needsRemap && !oldClsName.equals(clsName)) {
                        needsRemap = true;
                    }
                    size += clsName.b.remaining();
                    size += 2; // L and ;
                    break;
                default:
                    throw new RuntimeException("Illegal desc: " + desc.toString() + " " + ((char)character));
            }
        }
        if (!needsRemap) {
            return desc;
        }
        byte[] r = new byte[size];
        int pos = 0;
        for (int i = start; i < end; i++) {
            byte character = b.get(i);
            switch (character) {
                case '(':
                case ')':
                case 'V':
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                case '[':
                    r[pos++] = character;
                    break;
                case 'L':
                    r[pos++] = character;
                    int classEnd = -1;
                    for (int j = i + 1; j < end; j++) {
                        if (b.get(j) == ';') {
                            classEnd = j;
                            break;
                        }
                    }
                    Mutf8Slice oldClsName = new Mutf8Slice(ByteBufferUtil.slice(b, i + 1, classEnd));
                    i = classEnd;
                    Mutf8Slice clsName = mappings.mapClass(oldClsName);
                    ByteBuffer rb = clsName.b;
                    int remappedend = rb.limit();
                    for (int j = rb.position(); j < remappedend; j++) {
                        r[pos++] = rb.get(j);
                    }
                    r[pos++] = ';';
                    break;
                default:
                    throw new RuntimeException("Illegal desc: " + desc.toString());
            }
        }
        return new Mutf8Slice(ByteBuffer.wrap(r));
    }

    // Why are these so complicated >:(
    public static Mutf8Slice remapSignature(Mappings mappings, Mutf8Slice sig) {
        ByteBuffer b = sig.b;
        int start = b.position();
        int end = b.limit();
        ByteArrayList out = new ByteArrayList(sig.b.remaining() * 2);
        int pos = start;
        if (b.get(pos) == '<') {
            pos += 1;
            out.add((byte)'<');
            while (b.get(pos) != '>') {
                while (b.get(pos) != ':') {
                    out.add((byte)b.get(pos));
                    pos += 1;
                }
                while (b.get(pos) == ':') {
                    pos += 1;
                    out.add((byte)':');
                    if (b.get(pos) != ':') {
                        pos = remapReferenceTypeSignature(mappings, b, pos, out);
                    }
                }
            }
            pos += 1;
            out.add((byte)'>');
        }
        while (pos < end) {
            byte character = b.get(pos);
            if (character == 'L') {
                pos = remapClassTypeSignature(mappings, b, pos, out);
            } else if (character == 'T') {
                out.add((byte)'T');
                while (b.get(++pos) != ';') {
                    out.add(b.get(pos));
                }
                out.add((byte)';');
                pos += 1;
            } else {
                pos += 1;
                out.add(character);
            }
        }
        return new Mutf8Slice(ByteBuffer.wrap(out.elements(), 0, out.size()));
    }

    static int remapReferenceTypeSignature(Mappings mappings, ByteBuffer b, int pos, ByteArrayList out) {
        loop:
        for (;;pos++) {
            byte character = b.get(pos);
            switch (character) {
                case 'V':
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                    out.add(character);
                    pos += 1;
                    break loop;
                case '[':
                    out.add(character);
                    break; // switch
                case 'L':
                    pos = remapClassTypeSignature(mappings, b, pos, out);
                    break loop;
                case 'T':
                    out.add((byte)'T');
                    while (b.get(++pos) != ';') {
                        out.add(b.get(pos));
                    }
                    out.add((byte)';');
                    pos += 1;
                    break loop;
            }
        }
        return pos;
    }

    static int remapClassTypeSignature(Mappings mappings, ByteBuffer b, int pos, ByteArrayList out) {
        out.add((byte)'L');
        pos += 1;
        int startClass = pos;
        Mutf8Slice cls = null;
        boolean scanningInner = false;
        loop:
        for (;;pos++) {
            byte character = b.get(pos);
            if (character == '<' || character == '.' || character == ';') {
                if (cls == null) {
                    cls = new Mutf8Slice(ByteBufferUtil.slice(b, startClass, pos));
                    Mutf8Slice clsName = mappings.mapClass(cls);
                    out.ensureCapacity(out.size() + clsName.b.remaining());
                    for (int i = clsName.b.position(); i < clsName.b.limit(); i++) {
                        out.add(clsName.b.get(i));
                    }
                }
                if (scanningInner) {
                    int innerSize = pos - startClass;
                    ByteBuffer o = cls.b;
                    byte[] inner = new byte[o.remaining() + 1 + innerSize];
                    int ipos = 0;
                    int oend = o.limit();
                    for (int i = o.position(); i < oend; i++) {
                        inner[ipos++] = o.get(i);
                    }
                    inner[ipos++] = '$';
                    for (int i = startClass; i < pos; i++) {
                        inner[ipos++] = b.get(i);
                    }
                    cls = new Mutf8Slice(ByteBuffer.wrap(inner));
                    Mutf8Slice clsName = mappings.mapClass(cls);
                    for (int i = clsName.b.limit() - 1;;i--) {
                        if (clsName.b.get(i) == '$') {
                            for (int j = i + 1; j < clsName.b.limit(); j++) out.add(clsName.b.get(j));
                            break;
                        }
                    }
                    scanningInner = false;
                }
                out.add(character);
            }
            if (character == '.') {
                scanningInner = true;
                startClass = pos + 1;
            } else if (character == '<') {
                pos += 1;
                while ((character = b.get(pos)) != '>') {
                    if (character == '*' || character == '+' || character == '-') {
                        out.add(character);
                        pos += 1;
                    } else {
                        pos = remapReferenceTypeSignature(mappings, b, pos, out);
                    }
                }
                out.add((byte)'>');
            } else if (character == ';') {
                pos += 1;
                break loop;
            }
        }
        return pos;
    }
}
