package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.ConstantUtf8;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

class LazyAttributes extends Attributes {
    final ByteBuffer b;
    final RecombobulatorOptions options;
    final ConstantPool pool;
    final int major;
    final int minor;
    final AttributeType.Location location;
    Attribute[] read;
    int[] starts;
    int size = 0;

    final int endPos;

    LazyAttributes(ByteBuffer b, int pos, RecombobulatorOptions options, ConstantPool pool, int major, int minor, AttributeType.Location location) {
        this.b = b;
        this.options = options;
        this.pool = pool;
        this.major = major;
        this.minor = minor;
        this.location = location;
        size = u2(b, pos); // attributes_count
        pos += 2;
        read = new Attribute[size];
        starts = new int[size];
        for (int i = 0; i < size; i++) {
            starts[i] = pos;
            pos += 2; // attribute_name_index
            int attribute_length = (int) u4(b, pos);
            pos += 4;
            pos += attribute_length;
        }
        endPos = pos;
    }

    void ensureCapacity(int capacity) {
        if (read.length < capacity) {
            int newCap = Math.max(10, Math.max(read.length * 2, capacity));
            read = Arrays.copyOf(read, newCap);
            starts = Arrays.copyOf(starts, newCap);
        }
    }

    public void add(Attribute a) {
        if (size - read.length <= 0) {
            int newCap = Math.max(10, read.length * 2);
            read = Arrays.copyOf(read, newCap);
            starts = Arrays.copyOf(starts, newCap);
        }
        int index = size++;
        read[index] = a;
        starts[index] = -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Attribute get(int index) {
        Attribute readAttribute = read[index];
        if (readAttribute != null) return readAttribute;
        int pos = starts[index];
        int attribute_name_index = u2(b, pos);
        pos += 2;
        Mutf8Slice attribute_name = ((ConstantUtf8) pool.getEntry(attribute_name_index)).slice;
        AttributeType type = AttributeType.attributeMap.get(attribute_name);
        if (type == null || !type.supported(major, minor, location)) type = AttributeUnknown.TYPE;
        int attribute_length = (int) u4(b, pos); // can't be larger than max int size due to class size limit
        pos += 4;
        Attribute r = type.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
        read[index] = r;
        starts[index] = -1;
        return r;
    }

    @Override
    public void set(int index, Attribute a) {
        read[index] = a;
        starts[index] = -1;
    }

    @Override
    public void remove(int index) {
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(read, index+1, read, index, numMoved);
            System.arraycopy(starts, index+1, starts, index, numMoved);
        }
        read[--size] = null;
        // starts can be left as is since no gc reference
    }

    @Override
    public int byteSize() {
        int r = 2 /* attributes_count */ + (2 /* attribute_name_index */ + 4 /* attribute_length */) * size;
        for (int i = 0; i < size; i++) {
            int v = starts[i];
            if (v == -1) {
                r += read[i].byteSize();
            } else {
                r += (int) u4(b, v + 2); // attribute_length
            }
        }
        return r;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < size; i++) {
            Attribute e = get(i); // rip
            hashCode = 31*hashCode + e.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attributes) {
            Attributes o = (Attributes) obj;
            if (size != o.size()) return false;
            for (int i = 0; i < size; i++) {
                if (!get(i).equals(o.get(i))) return false; // rip
            }
            return true;
        }
        return false;
    }

    @Override
    public int readEnd() {
        return endPos;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short)size);
        for (int i = 0; i < size; i++) {
            int v = starts[i];
            if (v == -1) {
                Attribute a = read[i];
                o.writeShort((short) a.attribute_name_index);
                o.writeInt(a.byteSize());
                a.write(o);
            } else {
                int end = v + 2 /* attribute_name_index */ + 4 /* attribute_length */ + (int) u4(b, v + 2) /* attribute_length value */;
                o.writeBytes(slice(b, v, end));
            }
        }
    }

    @Override
    public Iterator<Attribute> iterator() {
        return new Iterator<Attribute>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < read.length;
            }

            @Override
            public Attribute next() {
                if (pos < read.length) {
                    return get(pos++);
                }
                throw new NoSuchElementException();
            }
        };
    }
}
