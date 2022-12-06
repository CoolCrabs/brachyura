package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantUnusable extends ConstantPoolEntry {
    public static ConstantUnusable INSTANCE = new ConstantUnusable();

    private ConstantUnusable() { }

    @Override
    byte tag() {
        throw new UnsupportedOperationException();
    }

    @Override
    int byteSize() {
        return -1;
    }

    @Override
    void write(RecombobulatorOutput o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
