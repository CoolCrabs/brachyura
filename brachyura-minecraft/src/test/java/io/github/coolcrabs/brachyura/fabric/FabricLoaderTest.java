package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class FabricLoaderTest {
    @Test
    void testDownloadFloader() {
        assertDoesNotThrow(() -> new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.9.3+build.207")));
    }
}
