package io.github.coolcrabs.brachyura.processing.sinks;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class ZipProcessingSink implements ProcessingSink, Closeable {
    // https://github.com/gradle/gradle/blob/master/subprojects/core/src/main/java/org/gradle/api/internal/file/archive/ZipCopyAction.java
    private static final FileTime MAGIC_TIME = FileTime.fromMillis(new GregorianCalendar(1980, Calendar.FEBRUARY, 1, 0, 0, 0).getTimeInMillis());

    final FileSystem fs;
    final TreeMap<ProcessingId, Supplier<InputStream>> entries = new TreeMap<>((a, b) -> a.path.compareTo(b.path));

    public ZipProcessingSink(Path zip) {
        this.fs = FileSystemUtil.newJarFileSystem(zip);
    }

    @Override
    public void sink(Supplier<InputStream> in, ProcessingId id) {
        if (entries.put(id, in) != null) throw new RuntimeException("Duplicate entries for: " + id.path);
    }

    @Override
    public void close() {
        try {
            for (Map.Entry<ProcessingId, Supplier<InputStream>> e : entries.entrySet()) {
                Path target = fs.getPath(e.getKey().path);
                Path parent = target.getParent();
                if (parent != null) {
                    for (int i = 1; i <= parent.getNameCount(); i++) {
                        Path d = parent.subpath(0, i);
                        if (!Files.isDirectory(d)) {
                            Files.createDirectory(d);
                            Files.getFileAttributeView(d, BasicFileAttributeView.class).setTimes(MAGIC_TIME, MAGIC_TIME, MAGIC_TIME);
                        }
                    }
                }
                try (InputStream i = e.getValue().get()) {
                    Files.copy(i, target);
                    Files.getFileAttributeView(target, BasicFileAttributeView.class).setTimes(MAGIC_TIME, MAGIC_TIME, MAGIC_TIME);
                }
            }
            fs.close();
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
    
}
