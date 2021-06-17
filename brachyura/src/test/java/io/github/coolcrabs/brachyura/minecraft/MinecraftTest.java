package io.github.coolcrabs.brachyura.minecraft;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class MinecraftTest {
    @Test
    void mcTest1_6_4() {
        VersionMeta meta = Minecraft.getVersion("1.6.4");
        assertNotNull(meta);
        Path client = Minecraft.getDownload("1.6.4", meta, "client");
        assertTrue(Files.isRegularFile(client));
        for (Path lib : Minecraft.getDependencies(meta)) {
            assertTrue(Files.isRegularFile(lib));
        }
    }

    @Test
    void mcTest1_17() {
        VersionMeta meta = Minecraft.getVersion("1.17");
        assertNotNull(meta);
        Path client = Minecraft.getDownload("1.17", meta, "server");
        assertTrue(Files.isRegularFile(client));
        for (Path lib : Minecraft.getDependencies(meta)) {
            assertTrue(Files.isRegularFile(lib));
        }
    }
}
