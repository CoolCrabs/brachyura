package io.github.coolcrabs.brachyura.decompiler;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class ReplaceLineNumberMappings implements LineNumberMappings {
    /**
     * Map between class name and its line number mappings.
     */
    public final Map<String, List<LineNumberMappingEntry>> mappings;

    public ReplaceLineNumberMappings(Map<String, List<LineNumberMappingEntry>> mappings) {
        this.mappings = mappings;
    }

    public static class LineNumberMappingEntry {
        public final String method;
        public final String descriptor;
        public final NavigableMap<Integer, Integer> lineNumbers;

        public LineNumberMappingEntry(String method, String descriptor, NavigableMap<Integer, Integer> lineNumbers) {
            this.method = method;
            this.descriptor = descriptor;
            this.lineNumbers = lineNumbers;
        }
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("v1\tReplaceLineNumberMappings\n");
        for (Map.Entry<String, List<LineNumberMappingEntry>> entry : mappings.entrySet()) {
            writer.write(entry.getKey());
            writer.write("\n");
            for (LineNumberMappingEntry mapping : entry.getValue()) {
                writer.write("\t");
                writer.write(mapping.method);
                writer.write("\t");
                writer.write(mapping.descriptor);
                writer.write("\n");
                for (Map.Entry<Integer, Integer> lineNumber : mapping.lineNumbers.entrySet()) {
                    writer.write("\t");
                    writer.write(lineNumber.getKey().toString());
                    writer.write(":");
                    writer.write(lineNumber.getValue().toString());
                }
                writer.write("\n");
            }
        }
    }
}
