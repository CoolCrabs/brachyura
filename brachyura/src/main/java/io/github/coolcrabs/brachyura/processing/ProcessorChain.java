package io.github.coolcrabs.brachyura.processing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.util.ArrayUtil;
import io.github.coolcrabs.brachyura.util.Util;
import java.util.Arrays;

public class ProcessorChain {
    final Processor[] processors;

    public ProcessorChain(Processor...processors) {
        this.processors = processors;
    }

    public ProcessorChain(ProcessorChain existing, Processor...processors) {
        this.processors = ArrayUtil.join(Processor.class, existing.processors, processors);
    }

    public void apply(ProcessingSink out, ProcessingSource... in) {
        apply(out, Arrays.asList(in));
    }

    public void apply(ProcessingSink out, Iterable<? extends ProcessingSource> in) {
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

    public Processor[] getProcessors() {
        return Arrays.copyOf(processors, processors.length);
    }

    static class Collector implements ProcessingSink {
        ArrayList<ProcessingEntry> e = new ArrayList<>();

        @Override
        public void sink(Supplier<InputStream> in, ProcessingId id) {
            e.add(new ProcessingEntry(in, id));
        }
    }
}
