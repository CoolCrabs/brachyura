package io.github.coolcrabs.brachyura.processing;

import java.io.InputStream;
import java.util.function.Supplier;

public class ProcessingEntry {
    public final Supplier<InputStream> in;
    public final ProcessingId id;

    public ProcessingEntry(Supplier<InputStream> in, ProcessingId id) {
        this.in = in;
        this.id = id;
    }
}
