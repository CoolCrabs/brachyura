package io.github.coolcrabs.brachyura.minecraft;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.dependency.NativesJarDependency;

class MinecraftTest {
    @Test
    void mcTest1_6_4() {
        VersionMeta meta = Minecraft.getVersion("1.6.4");
        assertNotNull(meta);
        Path client = Minecraft.getDownload("1.6.4", meta, "client");
        assertTrue(Files.isRegularFile(client));
        for (Dependency lib : Minecraft.getDependencies(meta)) {
            if (lib instanceof JavaJarDependency) {
                assertNotNull(((JavaJarDependency)lib).jar);
            } else if (lib instanceof NativesJarDependency) {
                assertNotNull(((NativesJarDependency)lib).jar);
            }
        }
    }

    @Test
    void mcTest1_17() {
        VersionMeta meta = Minecraft.getVersion("1.17");
        assertNotNull(meta);
        Path client = Minecraft.getDownload("1.17", meta, "server");
        assertTrue(Files.isRegularFile(client));
        for (Dependency lib : Minecraft.getDependencies(meta)) {
            if (lib instanceof JavaJarDependency) {
                assertNotNull(((JavaJarDependency)lib).jar);
            } else if (lib instanceof NativesJarDependency) {
                assertNotNull(((NativesJarDependency)lib).jar);
            }
        }
    }
}
