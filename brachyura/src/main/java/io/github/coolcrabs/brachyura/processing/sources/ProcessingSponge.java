package io.github.coolcrabs.brachyura.processing.sources;

import java.io.InputStream;
import java.util.HashMap;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;

// Lol
/**
 * Collects inputs and makes this it's new source
 * You can then retrieve certain paths and/or use it as a source
 */
public class ProcessingSponge extends ProcessingSource implements ProcessingSink {
    HashMap<String, ProcessingEntry> a = new HashMap<>();

    @Override
    public void sink(Supplier<InputStream> in, ProcessingId id) {
        a.put(id.path, new ProcessingEntry(in, new ProcessingId(id.path, this)));
    }

    @Override
    public void getInputs(ProcessingSink sink) {
        for (ProcessingEntry e : a.values()) {
            sink.sink(e.in, e.id);
        }
    }

    public ProcessingEntry popEntry(String path) {
        ProcessingEntry r = a.get(path);
        if (r != null) {
            a.remove(path);
        }
        return r;
    }
    
}
