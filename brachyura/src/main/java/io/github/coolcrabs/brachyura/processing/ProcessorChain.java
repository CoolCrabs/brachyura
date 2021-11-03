package io.github.coolcrabs.brachyura.processing;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.util.Util;

public class ProcessorChain {
    final Processor[] processors;

    public ProcessorChain(Processor...processors) {
        this.processors = processors;
    }

    public void apply(ProcessingSink out, ProcessingSource... in) {
        try {
            Collector c = new Collector();
            for (ProcessingSource s : in) {
                s.getInputs(c);
            }
            for (Processor p : processors) {
                Collector c2 = new Collector();
                p.process(c.e, c2);
                c = c2; 
            }
            for (ProcessingEntry pe : c.e) {
                out.sink(pe.in, pe.id);
            }
        } catch (IOException e) {
            Util.sneak(e);
        }
    }

    static class Collector implements ProcessingSink {
        ConcurrentLinkedQueue<ProcessingEntry> e = new ConcurrentLinkedQueue<>();

        @Override
        public void sink(Supplier<InputStream> in, ProcessingId id) {
            e.add(new ProcessingEntry(in, id));
        }
    }
}
