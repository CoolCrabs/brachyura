package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TypePath {
    public List<EntryPath> path;

    public TypePath(List<EntryPath> path) {
        this.path = path;
    }

    public int byteSize() {
        return 1 + (2 * path.size());
    }

    public void write(RecombobulatorOutput o) {
        o.writeByte((byte)path.size());
        for (EntryPath p : path) {
            p.write(o);
        }
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitTypePath(this);
        for (EntryPath ep : path) ep.accept(v);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypePath) {
            TypePath o = (TypePath) obj;
            return path.equals(o.path);
        }
        return false;
    }
}
