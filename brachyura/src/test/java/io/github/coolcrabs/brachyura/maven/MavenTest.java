package io.github.coolcrabs.brachyura.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class MavenTest {
    @Test
    void downloadFloader() {
        List<Path> floader = Maven.getMavenDep("https://maven.fabricmc.net/", new MavenId("net.fabricmc", "fabric-loader", "0.11.6"), ".jar", ".json");
        for (Path path : floader) {
            assertTrue(Files.isRegularFile(path));
        }
        assertEquals(2, floader.size());
    }

    @Test
    void noSources() {
        List<Path> oldtr = Maven.getMavenDep("https://maven.fabricmc.net", new MavenId("net.fabricmc", "tiny-remapper", "0.1.0.10"));
        for (Path path : oldtr) {
            assertTrue(Files.isRegularFile(path));
        }
        assertEquals(1, oldtr.size());
    }
}
