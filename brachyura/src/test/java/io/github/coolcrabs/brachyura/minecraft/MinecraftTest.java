package io.github.coolcrabs.brachyura.minecraft;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class MinecraftTest {
    @Test
    void mcTest1() {
        VersionMeta meta = Minecraft.getVersion("1.6.4");
        assertNotNull(meta);
        Path client = Minecraft.getDownload("1.6.4", meta, "client");
        assertTrue(Files.isRegularFile(client));
    }
}
