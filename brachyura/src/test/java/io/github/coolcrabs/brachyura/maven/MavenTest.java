package io.github.coolcrabs.brachyura.maven;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.dependency.FileDependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;

class MavenTest {
    @Test
    void downloadFloader() {
        JavaJarDependency floaderJar = Maven.getMavenJarDep("https://maven.fabricmc.net/", new MavenId("net.fabricmc", "fabric-loader", "0.11.6"));
        FileDependency floaderJson = (FileDependency) Maven.getMavenDep("https://maven.fabricmc.net/", new MavenId("net.fabricmc", "fabric-loader", "0.11.6"), ".json");
        assertTrue(Files.isRegularFile(floaderJar.jar));
        assertTrue(Files.isRegularFile(floaderJar.sourcesJar));
        assertTrue(Files.isRegularFile(floaderJson.file));
    }

    @Test
    void noSources() {
        JavaJarDependency oldtr = Maven.getMavenJarDep("https://maven.fabricmc.net", new MavenId("net.fabricmc", "tiny-remapper", "0.1.0.10"));
        assertTrue(Files.isRegularFile(oldtr.jar));
        assertNull(oldtr.sourcesJar);
    }
}
