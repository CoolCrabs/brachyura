package io.github.coolcrabs.brachyura.recombobulator.remapper.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class PathIsSup implements Supplier<InputStream> {
    public Path p;

    public PathIsSup(Path p) {
        this.p = p;
    }

    @Override
    public InputStream get() {
        try {
            return Files.newInputStream(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
