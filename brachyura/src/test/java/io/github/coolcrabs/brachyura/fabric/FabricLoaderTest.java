package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.maven.MavenId;

class FabricLoaderTest {
    @Test
    void testDownloadFloader() {
        assertDoesNotThrow(() -> new FabricLoader("https://maven.fabricmc.net/", new MavenId("net.fabricmc", "fabric-loader", "0.9.3+build.207")));
    }
}
