package io.github.coolcrabs.brachyura.recombobulator;

public abstract class ConstantPoolEntry {
    ConstantPoolEntry () { }

    abstract byte tag();

    /**
     * Size of entry excluding tag
     */
    abstract int byteSize();

    abstract void write(RecombobulatorOutput o);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
