package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class TargetEmpty extends Target {
    public TargetEmpty(byte tag) {
        super(tag);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitTargetEmpty(this);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetEmpty) {
            TargetEmpty o = (TargetEmpty) obj;
            return tag == o.tag;
        }
        return false;
    }
    
}
