package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import io.github.coolcrabs.brachyura.util.Util;

public class PathFileConsumer implements BiConsumer<String, byte[]> {
    private final Path root;

    public PathFileConsumer(Path path) {
        root = path;
    }

    @Override
    public void accept(String t, byte[] u) {
        try {
            Path target = root.resolve(t + ".class");
            Files.createDirectories(target.getParent());
            try (OutputStream stream = Files.newOutputStream(target)) {
                stream.write(u);
            }
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
    
}
