package io.github.coolcrabs.brachyura.processing;

public abstract class ProcessingSource {
    public abstract void getInputs(ProcessingSink sink);
    
    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }
}
