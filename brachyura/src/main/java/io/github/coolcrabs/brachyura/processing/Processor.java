package io.github.coolcrabs.brachyura.processing;

import java.io.IOException;
import java.util.Collection;

public interface Processor {
    void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException;
}
