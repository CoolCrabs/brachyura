package io.github.coolcrabs.brachyura.dependency;

import java.nio.file.Path;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

public class JavaJarDependency implements Dependency {
    public final Path jar;
    public final @Nullable Path sourcesJar;

    public JavaJarDependency(Path jar, @Nullable Path sourcesJar) {
        Objects.requireNonNull(jar);
        this.jar = jar;
        this.sourcesJar = sourcesJar;
    }
}
