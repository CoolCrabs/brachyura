package io.github.coolcrabs.brachyura.processing.sinks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.util.Util;

public class DirectoryProcessingSink implements ProcessingSink {
    final Path path;

    public DirectoryProcessingSink(Path path) {
        this.path = path;
    }

    @Override
    public void sink(Supplier<InputStream> in, ProcessingId id) {
        try {
            Path target = path.resolve(id.path);
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
}
