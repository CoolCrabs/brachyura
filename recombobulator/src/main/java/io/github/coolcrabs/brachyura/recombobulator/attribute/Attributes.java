package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;

import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;

public abstract class Attributes implements Iterable<Attribute> {
    Attributes() { }

    public static Attributes read(ByteBuffer b, int pos, RecombobulatorOptions options, ConstantPool pool, int major, int minor, AttributeType.Location location) {
        if (options.lazyAttributes) {
            return new LazyAttributes(b, pos, options, pool, major, minor, location);
        } else {
            return new EagerAttributes(b, pos, options, pool, major, minor, location);
        }
    }
    
    public abstract int size();

    public abstract Attribute get(int index);

    public abstract void set(int index, Attribute a);

    public abstract void remove(int index);

    public abstract void add(Attribute a);

    public abstract int byteSize();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public abstract int readEnd();

    public abstract void write(RecombobulatorOutput o);
}
