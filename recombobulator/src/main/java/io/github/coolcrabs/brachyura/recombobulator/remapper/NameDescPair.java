package io.github.coolcrabs.brachyura.recombobulator.remapper;

import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;

public class NameDescPair {
    Mutf8Slice name;
    Mutf8Slice desc;

    public NameDescPair(Mutf8Slice name, Mutf8Slice desc) {
        this.name = name;
        this.desc = desc;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + name.hashCode();
        result = 37*result + desc.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NameDescPair) {
            NameDescPair o = (NameDescPair) obj;
            return
                name.b.remaining() == o.name.b.remaining() &&
                desc.b.remaining() == o.desc.b.remaining() &&
                name.equals(o.name) &&
                desc.equals(o.desc);
        }
        return false;
    }

    @Override
    public String toString() {
        return name + " : " + desc;
    }
}
