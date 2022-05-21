package io.github.coolcrabs.brachyura.processing.sinks;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class ZipProcessingSink implements ProcessingSink, Closeable {
    // https://github.com/gradle/gradle/blob/master/subprojects/core/src/main/java/org/gradle/api/internal/file/archive/ZipCopyAction.java
    private static final long MAGIC_TIME = new GregorianCalendar(1980, Calendar.FEBRUARY, 1, 0, 0, 0).getTimeInMillis();

    final ZipOutputStream out;
    final TreeMap<ProcessingId, Supplier<InputStream>> entries = new TreeMap<>((a, b) -> a.path.compareTo(b.path));

    public ZipProcessingSink(Path zip) {
        this(PathUtil.outputStream(zip));
    }

    public ZipProcessingSink(OutputStream out) {
        this.out = new ZipOutputStream(out);
    }

    @Override
    public void sink(Supplier<InputStream> in, ProcessingId id) {
        if (entries.put(id, in) != null) throw new RuntimeException("Duplicate entries for: " + id.path);
    }

    @Override
    public void close() {
        try {
            for (Map.Entry<ProcessingId, Supplier<InputStream>> e : entries.entrySet()) {
                ZipEntry entry = new ZipEntry(e.getKey().path);
                entry.setTime(MAGIC_TIME);
                out.putNextEntry(entry);
                try (InputStream is = e.getValue().get()) {
                    StreamUtil.copy(is, out);
                }
            }
            out.close();
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
    
}
