package io.github.coolcrabs.brachyura.dependency;

import java.nio.file.Path;
import java.util.Objects;

public class NativesJarDependency implements Dependency {
    public final Path jar;

    public NativesJarDependency(Path jar) {
        Objects.requireNonNull(jar);
        this.jar = jar;
    }
}
