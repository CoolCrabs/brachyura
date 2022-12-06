package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public abstract class Target {
    public final byte tag;

    Target(byte tag) {
        this.tag = tag;
    }

    int byteSize() {
        return 1;
    }

    void write(RecombobulatorOutput o) {
        o.writeByte(tag);
    }

    public abstract void accept(RecombobulatorVisitor v);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
