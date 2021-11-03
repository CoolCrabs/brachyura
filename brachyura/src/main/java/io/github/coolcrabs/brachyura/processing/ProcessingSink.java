package io.github.coolcrabs.brachyura.processing;

import java.io.InputStream;
import java.util.function.Supplier;

public interface ProcessingSink {
    public void sink(Supplier<InputStream> in, ProcessingId id);
}
