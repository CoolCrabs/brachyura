package io.github.coolcrabs.brachyura.processing;

public final class ProcessingId {
    public final String path;
    public final ProcessingSource source;

    public ProcessingId(String path, ProcessingSource source) {
        this.path = path;
        this.source = source;
    }

    @Override
    public String toString() {
        return "ProcessingId [path=" + path + ", source=" + source + "]";
    }
}
