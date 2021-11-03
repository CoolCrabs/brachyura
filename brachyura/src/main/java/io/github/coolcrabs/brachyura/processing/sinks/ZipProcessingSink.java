package io.github.coolcrabs.brachyura.processing.sinks;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class ZipProcessingSink implements ProcessingSink, Closeable {
    final FileSystem fs;

    public ZipProcessingSink(Path zip) {
        this.fs = FileSystemUtil.newJarFileSystem(zip);
    }

    @Override
    public void sink(Supplier<InputStream> in, ProcessingId id) {
        try {
            Path target = fs.getPath(id.path);
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (InputStream i = in.get()) {
                Files.copy(i, target);
            }
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    @Override
    public void close() throws IOException {
        fs.close();
    }
    
}
