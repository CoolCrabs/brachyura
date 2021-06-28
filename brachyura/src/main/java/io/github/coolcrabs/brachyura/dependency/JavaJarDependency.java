package io.github.coolcrabs.brachyura.dependency;

import java.nio.file.Path;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.brachyura.maven.MavenId;

public class JavaJarDependency implements Dependency {
    public final Path jar;
    public final @Nullable Path sourcesJar;
    public final @Nullable MavenId mavenId;

    public JavaJarDependency(Path jar, @Nullable Path sourcesJar, @Nullable MavenId mavenId) {
        Objects.requireNonNull(jar);
        this.jar = jar;
        this.sourcesJar = sourcesJar;
        this.mavenId = mavenId;
    }
}
