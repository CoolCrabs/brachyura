package io.github.coolcrabs.brachyura.dependency;

import java.nio.file.Path;
import java.util.Objects;

public class FileDependency implements Dependency {
    public final Path file;

    public FileDependency(Path file) {
        Objects.requireNonNull(file);
        this.file = file;
    }
}
