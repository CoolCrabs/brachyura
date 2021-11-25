package io.github.coolcrabs.brachyura.processing.sources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;

public class FilteringProcessingSource extends ProcessingSource {
    final ProcessingSource parent;
    final Predicate<ProcessingEntry> filter;

    public FilteringProcessingSource(ProcessingSource parent, Predicate<ProcessingEntry> filter) {
        this.parent = parent;
        this.filter = filter;
    }

    @Override
    public void getInputs(ProcessingSink sink) {
        Collector c = new Collector();
        parent.getInputs(c);
        for (ProcessingEntry e : c.e) {
            if (filter.test(e)) sink.sink(e.in, e.id);
        }
    }
    
    static class Collector implements ProcessingSink {
        ArrayList<ProcessingEntry> e = new ArrayList<>();

        @Override
        public void sink(Supplier<InputStream> in, ProcessingId id) {
            e.add(new ProcessingEntry(in, id));
        }
    }
}
